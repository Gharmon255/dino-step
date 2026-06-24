-- One-time promo code redemptions (server-authoritative, per user per code)

create table if not exists public.promo_code_redemptions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users (id) on delete cascade,
  code text not null,
  reward_egg_rarity text not null,
  redeemed_at timestamptz not null default now(),
  unique (user_id, code)
);

create index if not exists promo_code_redemptions_user_id_idx
  on public.promo_code_redemptions (user_id);

alter table public.promo_code_redemptions enable row level security;

-- Clients redeem via Edge Function (service role). No direct client insert policies.
grant select, insert on public.promo_code_redemptions to service_role;
grant select, insert, update on public.game_saves to service_role;
