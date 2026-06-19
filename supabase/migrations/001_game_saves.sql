-- Stepasaurus cloud save schema (run in Supabase SQL editor)

create table if not exists public.profiles (
  id uuid primary key references auth.users (id) on delete cascade,
  display_name text,
  created_at timestamptz not null default now()
);

create table if not exists public.game_saves (
  user_id uuid primary key references auth.users (id) on delete cascade,
  schema_version int not null,
  revision bigint not null default 1,
  save_json jsonb not null,
  updated_at timestamptz not null default now()
);

create index if not exists game_saves_updated_at_idx on public.game_saves (updated_at desc);

alter table public.profiles enable row level security;
alter table public.game_saves enable row level security;

create policy "profiles_select_own"
  on public.profiles for select
  using (auth.uid() = id);

create policy "profiles_insert_own"
  on public.profiles for insert
  with check (auth.uid() = id);

create policy "profiles_update_own"
  on public.profiles for update
  using (auth.uid() = id);

create policy "game_saves_select_own"
  on public.game_saves for select
  using (auth.uid() = user_id);

create policy "game_saves_insert_own"
  on public.game_saves for insert
  with check (auth.uid() = user_id);

create policy "game_saves_update_own"
  on public.game_saves for update
  using (auth.uid() = user_id);

-- Required for PostgREST / Data API (especially if auto-expose is disabled)
grant usage on schema public to anon, authenticated;
grant select, insert, update on public.game_saves to authenticated;
grant select, insert, update on public.profiles to authenticated;

-- Auto-create profile on signup
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer set search_path = public
as $$
begin
  insert into public.profiles (id)
  values (new.id)
  on conflict (id) do nothing;
  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute procedure public.handle_new_user();
