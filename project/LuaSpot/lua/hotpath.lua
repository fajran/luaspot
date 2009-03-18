hotpath = {
	exchange = function()
		val = sunspot.accel_y()
		msg = "c hotpath insert " .. val

		luaspot.lock("hotpath")
		data = {}
		table.insert(data, { luaspot.get_node_addr(), val })
		luaspot.mem_set("hotpath", "table", data)
		luaspot.unlock("hotpath")

		luaspot.send_raw("broadcast", msg)
	end,

	insert = function(val)
		luaspot.lock("hotpath")
		data = luaspot.mem_get("hotpath", "table")
		table.insert(data, { luaspot.sender, tonumber(val) })
		table.sort(data, hotpath.sort)
		luaspot.mem_set("hotpath", "table", data)
		luaspot.unlock("hotpath")
	end,

	reset = function()
		luaspot.lock("hotpath")
		data = {}
		luaspot.mem_set("hotpath", "table", data)
		luaspot.unlock("hotpath")

		sunspot.led_rgb(1, 255, 0, 0)
		sunspot.led_on(1)
		sunspot.sleep(1000)
		sunspot.led_off(1)
	end,

	sort = function(a, b)
		if a[2] ~= b[2] then
			return tonumber(a[2]) < tonumber(b[2])
		end
		return a[1] <= b[1]
	end,

	dump = function()
		data = luaspot.mem_get("hotpath", "table")
		res = "c debug print"
		addr = luaspot.get_node_addr()

		if data == nil then
			res = res .. " --"
		else
			for i,v in pairs(data) do
				res = res .. " " .. v[1] .. "=" .. v[2]
			end
		end

		luaspot.send_raw("broadcast", res)
	end,

	get_next = function()
		addr = luaspot.get_node_addr()
		data = luaspot.mem_get("hotpath", "table")

		if data == nil then
			print("data table is null")
			return ""
		end
		print("data table is not null")

		last = ""
		res = ""
		for i,v in pairs(data) do
			if last ~= "" then
				res = v[1]
				break
			end
			if v[1] == addr then
				last = v[1]
			end
		end

		print("next=" .. res)

		return res
	end,

	off = function()
		sunspot.led_off(7)
	end,

	on = function()
		sunspot.led_rgb(7, 255, 255, 0)
		sunspot.led_on(7)
	end,

	showpath = function()
		addr = luaspot.get_node_addr()
		data = luaspot.mem_get("hotpath", "table")

		if data ~= nil then
			i,v = pairs(data[1])
			if v[1] == addr then
				hotpath.seqblink()
			end
		end
		
	end,

	seqblink = function()
		hotpath.on()

		addr = luaspot.get_node_addr()
		nextaddr = hotpath.get_next()
		sunspot.sleep(500)

		luaspot.send_raw("broadcast", "c debug print seqblink: curr=" .. addr .. " next=" .. nextaddr)

		sunspot.sleep(500)

		if nextaddr ~= "" then
			luaspot.send_raw(nextaddr, "c hotpath seqblink")
		end
	end,

}

