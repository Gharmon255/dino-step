-- Per-challenge short join codes (5 chars) for friend battles

alter table public.battle_challenges
  add column if not exists join_code text;

create unique index if not exists battle_challenges_join_code_unique_idx
  on public.battle_challenges (join_code)
  where join_code is not null;

create index if not exists battle_challenges_join_code_pending_idx
  on public.battle_challenges (join_code)
  where status = 'pending';
