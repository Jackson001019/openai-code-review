curl --location 'https://open.bigmodel.cn/api/paas/v4/chat/completions' \
--header 'Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsInNpZ25fdHlwZSI6IlNJR04ifQ.eyJhcGlfa2V5IjoiNTRlYzUxNTc0MDg1NDdjMWIzODc3ZDNmMTVlNjc1OWEiLCJleHAiOjE3NTA4MzYxOTk4MTksInRpbWVzdGFtcCI6MTc1MDgzNDM5OTgyNH0.bEwPGxK9KubODEfEhy-GD0njD6ojzl67UXRMRTx3aS4' \
--header 'Content-Type: application/json' \
--data '{
    "model": "glm-4",
    "messages": [
        {
            "role": "user",
            "content": "你好,1+1等于多少？"
        }
    ]
}'