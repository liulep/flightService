local key = KEYS[1]
local number = tonumber(ARGV[1])
if (redis.call("exists", key) == 0) then
    return -2
end
local num = tonumber(redis.call('get', key))
if (num == 0) then
    return -1
elseif (num < number) then
    return 0
else
    redis.call("decrby", key, number)
    return 1
end
