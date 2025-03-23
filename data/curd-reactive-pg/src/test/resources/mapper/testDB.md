## fetchOne
> test
```sql
select #a#::text as underline_to_camel
```

## one
> test
```sql
SELECT id, user_id, nickname, avatar, realname, mobile, 
address, birthday, tag, create_time, create_user_id, update_time, update_user_id
FROM "user"
WHERE id=#id#
```

## save1
> test
```sql
insert into "user" (
user_id,
nickname,
avatar,
realname,
mobile,
address,
birthday,
create_user_id,
update_user_id)
values(
#userId#,
#nickname#,
#avatar#,
#realname#,
#mobile#,
#address#,
#birthday#,
#createUserId#,
#updateUserId#
)
returning id
```

## save2
> test
```sql
insert into account_psw (
user_id,
psw,
create_user_id,
update_user_id
)
values(
#userId#,
#psw#,
#createUserId#,
#updateUserId#
)
returning id
```

## save3
> test
```sql
insert into user_role (
user_id,
role_list,
create_user_id,
update_user_id)
values(
#userId#,
#roleList#::jsonb,
#createUserId#,
#updateUserId#
)
returning id
```

## save4
> test
```sql
insert into watermark (
user_id,
config,
used_count,
create_user_id,
update_user_id)
values(
#userId#,
#config#::jsonb,
#usedCount#,
#createUserId#,
#updateUserId#
)
returning id
```
