import { serve } from "https://deno.land/std@0.224.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.49.1";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

/** Normalized code -> egg rarity granted on next reward claim */
const PROMO_CODES: Record<string, string> = {
  epic20: "EPIC",
};

function jsonResponse(body: Record<string, unknown>, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });
}

serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  if (req.method !== "POST") {
    return jsonResponse({ error: "Method not allowed" }, 405);
  }

  try {
    const supabaseUrl = Deno.env.get("SUPABASE_URL");
    const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
    const anonKey = Deno.env.get("SUPABASE_ANON_KEY");
    if (!supabaseUrl || !serviceRoleKey || !anonKey) {
      return jsonResponse({ error: "Server misconfigured" }, 500);
    }

    const authHeader = req.headers.get("Authorization");
    if (!authHeader) {
      return jsonResponse({ error: "Sign in required" }, 401);
    }

    const userClient = createClient(supabaseUrl, anonKey, {
      global: { headers: { Authorization: authHeader } },
    });
    const { data: userData, error: userError } = await userClient.auth.getUser();
    if (userError || !userData.user) {
      return jsonResponse({ error: "Invalid session" }, 401);
    }
    const userId = userData.user.id;

    const body = await req.json().catch(() => ({}));
    const action = typeof body.action === "string" ? body.action : "redeem";
    const rawCode = typeof body.code === "string" ? body.code : "";
    const code = rawCode.trim().toLowerCase();
    const rewardRarity = PROMO_CODES[code];

    if (!rewardRarity) {
      return jsonResponse({ error: "Unknown or invalid promo code" }, 400);
    }

    const admin = createClient(supabaseUrl, serviceRoleKey);

    if (action === "status") {
      const { data: existing } = await admin
        .from("promo_code_redemptions")
        .select("redeemed_at, reward_egg_rarity")
        .eq("user_id", userId)
        .eq("code", code)
        .maybeSingle();

      return jsonResponse({
        code,
        redeemed: existing != null,
        rewardEggRarity: existing?.reward_egg_rarity ?? rewardRarity,
        redeemedAt: existing?.redeemed_at ?? null,
      });
    }

    const { data: existing } = await admin
      .from("promo_code_redemptions")
      .select("id")
      .eq("user_id", userId)
      .eq("code", code)
      .maybeSingle();

    if (existing) {
      return jsonResponse({ error: "This promo code was already used on your account" }, 409);
    }

    const { error: insertError } = await admin.from("promo_code_redemptions").insert({
      user_id: userId,
      code,
      reward_egg_rarity: rewardRarity,
    });

    if (insertError) {
      if (insertError.code === "23505") {
        return jsonResponse({ error: "This promo code was already used on your account" }, 409);
      }
      return jsonResponse({ error: insertError.message }, 500);
    }

    const { data: saveRow } = await admin
      .from("game_saves")
      .select("save_json, revision, schema_version")
      .eq("user_id", userId)
      .maybeSingle();

    if (saveRow?.save_json) {
      const saveJson = { ...saveRow.save_json, pendingRewardEggRarity: rewardRarity };
      const { error: updateError } = await admin
        .from("game_saves")
        .update({
          save_json: saveJson,
          revision: Number(saveRow.revision ?? 0) + 1,
          updated_at: new Date().toISOString(),
        })
        .eq("user_id", userId);

      if (updateError) {
        console.error("Failed to update game_saves after promo redeem", updateError);
      }
    }

    return jsonResponse({
      ok: true,
      code,
      pendingRewardEggRarity: rewardRarity,
      message: "Your next reward egg will be Epic!",
    });
  } catch (error) {
    const message = error instanceof Error ? error.message : "Unknown error";
    return jsonResponse({ error: message }, 500);
  }
});
