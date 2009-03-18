router2 = {
	exchange = function()
		val = 0
		msg = "c router2 insert " .. val

		luaspot.lock("router2")
		data = {}
		table.insert(data, { luaspot.get_node_addr(), val })
		luaspot.mem_set("router2", "table", data)
		luaspot.unlock("router2")

		luaspot.send_raw("broadcast", msg)
	end,

	insert = function(val)
		luaspot.lock("router2")
		data = luaspot.mem_get("router2", "table")
		table.insert(data, { luaspot.sender, tonumber(val) })
		table.sort(data, router2.sort)
		luaspot.mem_set("router2", "table", data)
		luaspot.unlock("router2")
	end,

	sort = function(a, b)
		if a[2] ~= b[2] then
			return a[2] < b[2]
		end
		return a[1] <= b[1]
	end,

	get_next = function()
		addr = luaspot.get_node_addr()
		data = luaspot.mem_get("router2", "table")
		last = ""
		res = ""
		for i,v = pairs(data) do
			if last ~= "" then
				res = last
				break
			end
			if v[1] == addr then
				last = v[1]
			end
		end

		return res
	end,

	off = function()
		luaspot.led_off(7)
	end,

	seqblink = function()
		luaspot.led_rgb(7, 255, 255, 0)
		luaspot.led_on(7)

		sunspot.sleep(2000)

		nextaddr = router2.get_next
		if nextaddr ~= "" then
			luaspot.send_raw(nextaddr, "c router2 seqblink")
		end
	end,



}

