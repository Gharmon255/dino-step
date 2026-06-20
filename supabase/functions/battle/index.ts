import { serve } from "https://deno.land/std@0.224.0/http/server.ts";
import { createClient, SupabaseClient } from "https://esm.sh/@supabase/supabase-js@2.49.1";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

const JOIN_CODE_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
const JOIN_CODE_LENGTH = 5;

const speciesBasePower: Record<string, number> = {
  COMMON: 100,
  UNCOMMON: 130,
  RARE: 170,
  EPIC: 220,
  LEGENDARY: 280,
};

const eggBonus: Record<string, number> = {
  COMMON: 0,
  UNCOMMON: 10,
  RARE: 20,
  EPIC: 30,
  LEGENDARY: 40,
};

type CompletedEntry = {
  id: string;
  speciesId: string;
  eggRarityAtHatch?: string;
  exLevel?: number;
};

type FighterPick = {
  completedCreatureId: string;
  speciesId: string;
};

function packMultiplier(packCount: number): number {
  if (packCount <= 1) return 1;
  return 1 + Math.min(packCount - 1, 3) * 0.15;
}

function computePower(
  fighter: CompletedEntry,
  collection: CompletedEntry[],
  speciesRarity: string,
) {
  const base = speciesBasePower[speciesRarity] ?? 100;
  const egg = eggBonus[fighter.eggRarityAtHatch ?? "COMMON"] ?? 0;
  const exLevel = Math.max(1, fighter.exLevel ?? 1);
  const packCount = collection.filter((c) => c.speciesId === fighter.speciesId).length;
  const combatPower = Math.floor((base + egg + exLevel * 3) * packMultiplier(packCount));
  return {
    combatPower,
    maxHp: Math.floor(combatPower * 1.2),
    attack: Math.max(1, Math.floor(combatPower * 0.35)),
    packCount,
    exLevel,
  };
}

function resolveBattle(
  aName: string,
  bName: string,
  aStats: { maxHp: number; attack: number; packCount: number },
  bStats: { maxHp: number; attack: number; packCount: number },
) {
  let aHp = aStats.maxHp;
  let bHp = bStats.maxHp;
  const turnLog: Array<Record<string, unknown>> = [];
  let turn = 0;
  let attackerIsA = true;

  while (aHp > 0 && bHp > 0 && turn < 20) {
    turn += 1;
    if (attackerIsA) {
      const damage = aStats.attack;
      bHp = Math.max(0, bHp - damage);
      turnLog.push({
        turn,
        actor: "a",
        action: aStats.packCount > 1 ? "Pack Strike" : "Attack",
        target: "b",
        damage,
        aHp,
        bHp,
        message: `${aName} attacked for ${damage} damage`,
      });
    } else {
      const damage = bStats.attack;
      aHp = Math.max(0, aHp - damage);
      turnLog.push({
        turn,
        actor: "b",
        action: bStats.packCount > 1 ? "Pack Strike" : "Attack",
        target: "a",
        damage,
        aHp,
        bHp,
        message: `${bName} attacked for ${damage} damage`,
      });
    }
    attackerIsA = !attackerIsA;
  }

  let winner: "a" | "b" | "draw" = "draw";
  if (aHp > bHp) winner = "a";
  else if (bHp > aHp) winner = "b";

  return { winner, turnLog };
}

function findFighter(saveJson: Record<string, unknown>, completedCreatureId: string): CompletedEntry | null {
  const completed = (saveJson.completedCreatures as CompletedEntry[]) ?? [];
  return completed.find((entry) => entry.id === completedCreatureId) ?? null;
}

function speciesRarityFromCatalog(speciesId: string): string {
  const rare = ["trex", "spinosaurus", "allosaurus", "mosasaurus", "diplodocus", "velociraptor_alpha", "therizinosaurus"];
  const epic = ["giganotosaurus", "quetzalcoatlus", "indominus_hybrid", "ancient_spinosaurus"];
  const legendary = ["frost_raptor", "volcanic_t_rex", "shadow_triceratops", "titanosaur", "cosmic_pterodactyl", "ancient_apex_rex"];
  if (legendary.includes(speciesId)) return "LEGENDARY";
  if (epic.includes(speciesId)) return "EPIC";
  if (rare.includes(speciesId)) return "RARE";
  const uncommon = ["stegosaurus", "pteranodon", "brachiosaurus", "dilophosaurus", "iguanodon", "carnotaurus", "baryonyx", "plesiosaurus"];
  if (uncommon.includes(speciesId)) return "UNCOMMON";
  return "COMMON";
}

function generateJoinCode(): string {
  let code = "";
  for (let i = 0; i < JOIN_CODE_LENGTH; i++) {
    code += JOIN_CODE_CHARS[Math.floor(Math.random() * JOIN_CODE_CHARS.length)];
  }
  return code;
}

async function generateUniqueJoinCode(admin: SupabaseClient): Promise<string> {
  for (let attempt = 0; attempt < 20; attempt++) {
    const code = generateJoinCode();
    const { data } = await admin
      .from("battle_challenges")
      .select("id")
      .eq("join_code", code)
      .maybeSingle();
    if (!data) return code;
  }
  throw new Error("Could not generate battle code");
}

async function loadSave(admin: SupabaseClient, userId: string) {
  const { data, error } = await admin.from("game_saves").select("save_json").eq("user_id", userId).maybeSingle();
  if (error) throw error;
  if (!data?.save_json) throw new Error("Cloud save required for battles");
  return data.save_json as Record<string, unknown>;
}

serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const supabaseUrl = Deno.env.get("SUPABASE_URL")!;
    const supabaseAnonKey = Deno.env.get("SUPABASE_ANON_KEY")!;
    const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
    const authHeader = req.headers.get("Authorization");
    if (!authHeader) {
      return new Response(JSON.stringify({ error: "Missing authorization" }), {
        status: 401,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    const authClient = createClient(supabaseUrl, supabaseAnonKey, {
      global: { headers: { Authorization: authHeader } },
    });
    const admin = createClient(supabaseUrl, serviceRoleKey);

    const { data: userData, error: userError } = await authClient.auth.getUser();
    if (userError || !userData.user) {
      return new Response(JSON.stringify({ error: "Unauthorized" }), {
        status: 401,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }
    const userId = userData.user.id;

    const body = await req.json();
    const action = body.action as string;

    if (action === "createChallenge") {
      await admin
        .from("battle_challenges")
        .update({ status: "cancelled" })
        .eq("challenger_id", userId)
        .eq("status", "pending");

      const joinCode = await generateUniqueJoinCode(admin);
      const { data: challenge, error } = await admin
        .from("battle_challenges")
        .insert({ challenger_id: userId, status: "pending", join_code: joinCode })
        .select("*")
        .single();
      if (error) throw error;
      return jsonResponse({ challenge, inviteCode: joinCode });
    }

    if (action === "acceptChallenge") {
      const joinCode = String(body.inviteCode ?? "").trim().toUpperCase();
      if (joinCode.length !== JOIN_CODE_LENGTH) {
        throw new Error(`Battle code must be ${JOIN_CODE_LENGTH} characters`);
      }

      const { data: pendingChallenge, error: pendingError } = await admin
        .from("battle_challenges")
        .select("*")
        .eq("join_code", joinCode)
        .eq("status", "pending")
        .maybeSingle();
      if (pendingError) throw pendingError;
      if (!pendingChallenge) {
        throw new Error("Invalid or expired battle code");
      }
      if (pendingChallenge.challenger_id === userId) {
        throw new Error("Enter your opponent's battle code, not yours");
      }
      if (pendingChallenge.opponent_id) {
        throw new Error("This battle already has an opponent");
      }

      const { data: challenge, error } = await admin
        .from("battle_challenges")
        .update({ opponent_id: userId, status: "picking" })
        .eq("id", pendingChallenge.id)
        .eq("status", "pending")
        .select("*")
        .single();
      if (error) throw error;
      return jsonResponse({ challenge });
    }

    if (action === "joinChallenge") {
      const challengeId = body.challengeId as string;
      const { data: challenge, error } = await admin
        .from("battle_challenges")
        .update({ opponent_id: userId, status: "picking" })
        .eq("id", challengeId)
        .eq("status", "pending")
        .neq("challenger_id", userId)
        .select("*")
        .maybeSingle();
      if (error) throw error;
      if (!challenge) throw new Error("Challenge unavailable");
      return jsonResponse({ challenge });
    }

    if (action === "submitPick") {
      const challengeId = body.challengeId as string;
      const completedCreatureId = String(body.completedCreatureId);
      const saveJson = await loadSave(admin, userId);
      const fighter = findFighter(saveJson, completedCreatureId);
      if (!fighter) throw new Error("Fighter not owned");

      const pick: FighterPick = {
        completedCreatureId,
        speciesId: fighter.speciesId,
      };

      const { data: existing, error: fetchError } = await admin
        .from("battle_challenges")
        .select("*")
        .eq("id", challengeId)
        .maybeSingle();
      if (fetchError) throw fetchError;
      if (!existing) throw new Error("Challenge not found");
      if (existing.status !== "picking" && existing.status !== "accepted") {
        throw new Error("Challenge not ready for picks");
      }

      const isChallenger = existing.challenger_id === userId;
      const isOpponent = existing.opponent_id === userId;
      if (!isChallenger && !isOpponent) throw new Error("Not a participant");

      const updatePayload: Record<string, unknown> = { status: "picking" };
      if (isChallenger) updatePayload.challenger_pick = pick;
      if (isOpponent) updatePayload.opponent_pick = pick;

      const { data: updated, error: updateError } = await admin
        .from("battle_challenges")
        .update(updatePayload)
        .eq("id", challengeId)
        .select("*")
        .single();
      if (updateError) throw updateError;

      const challengerPick = (isChallenger ? pick : updated.challenger_pick) as FighterPick | null;
      const opponentPick = (isOpponent ? pick : updated.opponent_pick) as FighterPick | null;

      if (!challengerPick || !opponentPick) {
        return jsonResponse({ challenge: updated, battle: null, waiting: true });
      }

      const challengerSave = await loadSave(admin, updated.challenger_id);
      const opponentSave = await loadSave(admin, updated.opponent_id!);
      const aFighter = findFighter(challengerSave, challengerPick.completedCreatureId)!;
      const bFighter = findFighter(opponentSave, opponentPick.completedCreatureId)!;
      const aCollection = (challengerSave.completedCreatures as CompletedEntry[]) ?? [];
      const bCollection = (opponentSave.completedCreatures as CompletedEntry[]) ?? [];

      const aStats = computePower(aFighter, aCollection, speciesRarityFromCatalog(aFighter.speciesId));
      const bStats = computePower(bFighter, bCollection, speciesRarityFromCatalog(bFighter.speciesId));
      const outcome = resolveBattle(aFighter.speciesId, bFighter.speciesId, aStats, bStats);

      const { data: battle, error: battleError } = await admin
        .from("battles")
        .insert({
          mode: "friend",
          player_a_user_id: updated.challenger_id,
          player_b_user_id: updated.opponent_id,
          player_a_species_id: aFighter.speciesId,
          player_b_species_id: bFighter.speciesId,
          player_a_completed_creature_id: challengerPick.completedCreatureId,
          player_b_completed_creature_id: opponentPick.completedCreatureId,
          player_a_power: aStats.combatPower,
          player_b_power: bStats.combatPower,
          player_a_pack_count: aStats.packCount,
          player_b_pack_count: bStats.packCount,
          player_a_ex_level: aStats.exLevel,
          player_b_ex_level: bStats.exLevel,
          winner: outcome.winner,
          turn_log: outcome.turnLog,
        })
        .select("*")
        .single();
      if (battleError) throw battleError;

      await admin
        .from("battle_challenges")
        .update({ status: "complete", battle_id: battle.id })
        .eq("id", challengeId);

      return jsonResponse({ challenge: { ...updated, status: "complete" }, battle, waiting: false });
    }

    if (action === "findQuickMatch") {
      const completedCreatureId = String(body.completedCreatureId);
      const saveJson = await loadSave(admin, userId);
      const fighter = findFighter(saveJson, completedCreatureId);
      if (!fighter) throw new Error("Fighter not owned");

      const collection = (saveJson.completedCreatures as CompletedEntry[]) ?? [];
      const myStats = computePower(fighter, collection, speciesRarityFromCatalog(fighter.speciesId));

      const { data: opponents, error: oppError } = await admin
        .from("game_saves")
        .select("user_id, save_json")
        .neq("user_id", userId)
        .limit(20);
      if (oppError) throw oppError;

      let ghostSave = opponents?.[0]?.save_json as Record<string, unknown> | undefined;
      if (!ghostSave) {
        ghostSave = {
          completedCreatures: [{
            id: "ghost-1",
            speciesId: "trex",
            eggRarityAtHatch: "RARE",
            exLevel: 5,
          }],
        };
      }
      const ghostCollection = (ghostSave.completedCreatures as CompletedEntry[]) ?? [];
      const ghostFighter = ghostCollection[0];
      const ghostStats = computePower(ghostFighter, ghostCollection, speciesRarityFromCatalog(ghostFighter.speciesId));
      const outcome = resolveBattle(fighter.speciesId, ghostFighter.speciesId, myStats, ghostStats);

      const ghostUserId = opponents?.[0]?.user_id ?? userId;
      const { data: battle, error: battleError } = await admin
        .from("battles")
        .insert({
          mode: "quick_match",
          player_a_user_id: userId,
          player_b_user_id: ghostUserId,
          player_a_species_id: fighter.speciesId,
          player_b_species_id: ghostFighter.speciesId,
          player_a_completed_creature_id: completedCreatureId,
          player_b_completed_creature_id: ghostFighter.id,
          player_a_power: myStats.combatPower,
          player_b_power: ghostStats.combatPower,
          player_a_pack_count: myStats.packCount,
          player_b_pack_count: ghostStats.packCount,
          player_a_ex_level: myStats.exLevel,
          player_b_ex_level: ghostStats.exLevel,
          winner: outcome.winner,
          turn_log: outcome.turnLog,
        })
        .select("*")
        .single();
      if (battleError) throw battleError;

      return jsonResponse({ battle });
    }

    if (action === "listBattles") {
      const { data, error } = await admin
        .from("battles")
        .select("*")
        .or(`player_a_user_id.eq.${userId},player_b_user_id.eq.${userId}`)
        .order("created_at", { ascending: false })
        .limit(20);
      if (error) throw error;
      return jsonResponse({ battles: data ?? [] });
    }

    if (action === "getChallenge") {
      const challengeId = body.challengeId as string;
      const { data, error } = await admin
        .from("battle_challenges")
        .select("*")
        .eq("id", challengeId)
        .maybeSingle();
      if (error) throw error;
      if (!data) throw new Error("Challenge not found");
      if (data.challenger_id !== userId && data.opponent_id !== userId) {
        throw new Error("Not a participant");
      }

      const sanitized = { ...data };
      if (data.challenger_id !== userId) sanitized.challenger_pick = data.challenger_pick ? { hidden: true } : null;
      if (data.opponent_id !== userId) sanitized.opponent_pick = data.opponent_pick ? { hidden: true } : null;
      return jsonResponse({ challenge: sanitized });
    }

    if (action === "getBattle") {
      const battleId = body.battleId as string;
      const { data, error } = await admin
        .from("battles")
        .select("*")
        .eq("id", battleId)
        .maybeSingle();
      if (error) throw error;
      if (!data) throw new Error("Battle not found");
      if (data.player_a_user_id !== userId && data.player_b_user_id !== userId) {
        throw new Error("Not a participant");
      }
      return jsonResponse({ battle: data });
    }

    throw new Error(`Unknown action: ${action}`);
  } catch (error) {
    console.error("battle function error:", error);
    const message = formatError(error);
    return new Response(JSON.stringify({ error: message }), {
      status: 400,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  }
});

function formatError(error: unknown): string {
  if (error instanceof Error && error.message) {
    return error.message;
  }
  if (error && typeof error === "object") {
    const record = error as Record<string, unknown>;
    const parts = [record.message, record.details, record.hint, record.code]
      .filter((part) => part != null && String(part).length > 0)
      .map(String);
    if (parts.length > 0) {
      return parts.join(" — ");
    }
    try {
      return JSON.stringify(error);
    } catch {
      return String(error);
    }
  }
  return error == null ? "Unknown error" : String(error);
}

function jsonResponse(payload: Record<string, unknown>) {
  return new Response(JSON.stringify(payload), {
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });
}
