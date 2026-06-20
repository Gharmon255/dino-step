-- Edge Functions use the service_role JWT. RLS is bypassed, but Postgres table
-- privileges are still required (see PostgREST error 42501).

grant select, insert, update on public.battle_challenges to service_role;
grant select, insert, update on public.battles to service_role;
grant select on public.game_saves to service_role;
