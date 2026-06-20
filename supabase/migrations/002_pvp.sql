-- Stepasaurus PvP schema (Phase 2)

create table if not exists public.player_profiles (
  user_id uuid primary key references auth.users (id) on delete cascade,
  display_name text,
  invite_code text not null unique,
  elo int not null default 1000,
  created_at timestamptz not null default now()
);

create table if not exists public.battle_challenges (
  id uuid primary key default gen_random_uuid(),
  challenger_id uuid not null references auth.users (id) on delete cascade,
  opponent_id uuid references auth.users (id) on delete cascade,
  status text not null default 'pending',
  challenger_pick jsonb,
  opponent_pick jsonb,
  battle_id uuid,
  expires_at timestamptz not null default (now() + interval '24 hours'),
  created_at timestamptz not null default now(),
  constraint battle_challenges_status_check check (
    status in ('pending', 'accepted', 'picking', 'revealed', 'complete', 'expired', 'cancelled')
  )
);

create index if not exists battle_challenges_challenger_idx on public.battle_challenges (challenger_id);
create index if not exists battle_challenges_opponent_idx on public.battle_challenges (opponent_id);
create index if not exists battle_challenges_status_idx on public.battle_challenges (status);

create table if not exists public.battles (
  id uuid primary key default gen_random_uuid(),
  mode text not null,
  player_a_user_id uuid not null references auth.users (id) on delete cascade,
  player_b_user_id uuid not null references auth.users (id) on delete cascade,
  player_a_species_id text not null,
  player_b_species_id text not null,
  player_a_completed_creature_id text not null,
  player_b_completed_creature_id text not null,
  player_a_power int not null,
  player_b_power int not null,
  player_a_pack_count int not null default 1,
  player_b_pack_count int not null default 1,
  player_a_ex_level int not null default 1,
  player_b_ex_level int not null default 1,
  winner text not null,
  turn_log jsonb not null default '[]'::jsonb,
  elo_delta int not null default 0,
  created_at timestamptz not null default now(),
  constraint battles_mode_check check (mode in ('quick_match', 'friend')),
  constraint battles_winner_check check (winner in ('a', 'b', 'draw'))
);

create index if not exists battles_player_a_idx on public.battles (player_a_user_id, created_at desc);
create index if not exists battles_player_b_idx on public.battles (player_b_user_id, created_at desc);

alter table public.player_profiles enable row level security;
alter table public.battle_challenges enable row level security;
alter table public.battles enable row level security;

create policy "player_profiles_select_own"
  on public.player_profiles for select
  using (auth.uid() = user_id);

create policy "player_profiles_insert_own"
  on public.player_profiles for insert
  with check (auth.uid() = user_id);

create policy "player_profiles_update_own"
  on public.player_profiles for update
  using (auth.uid() = user_id);

create policy "battle_challenges_select_participant"
  on public.battle_challenges for select
  using (auth.uid() = challenger_id or auth.uid() = opponent_id);

create policy "battle_challenges_insert_challenger"
  on public.battle_challenges for insert
  with check (auth.uid() = challenger_id);

create policy "battle_challenges_update_participant"
  on public.battle_challenges for update
  using (auth.uid() = challenger_id or auth.uid() = opponent_id);

create policy "battles_select_participant"
  on public.battles for select
  using (auth.uid() = player_a_user_id or auth.uid() = player_b_user_id);

grant usage on schema public to anon, authenticated;
grant select, insert, update on public.player_profiles to authenticated;
grant select, insert, update on public.battle_challenges to authenticated;
grant select, insert on public.battles to authenticated;

-- Auto-create player profile + invite code on signup
create or replace function public.handle_new_battle_player()
returns trigger
language plpgsql
security definer set search_path = public
as $$
begin
  insert into public.player_profiles (user_id, invite_code)
  values (
    new.id,
    upper(substr(replace(gen_random_uuid()::text, '-', ''), 1, 8))
  )
  on conflict (user_id) do nothing;
  return new;
end;
$$;

drop trigger if exists on_auth_user_created_battle on auth.users;
create trigger on_auth_user_created_battle
  after insert on auth.users
  for each row execute procedure public.handle_new_battle_player();
