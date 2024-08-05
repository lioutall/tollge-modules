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