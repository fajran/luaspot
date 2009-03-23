demo = {
	discover = function()
		luaspot.send("broadcast", "c demo introduce " .. luaspot.sender, 1)
	end,

	introduce = function(bsaddr)
		luaspot.send_raw(luaspot.sender, "c demo report " .. bsaddr)
	end,

	report = function(bsaddr)
		luaspot.send_raw(bsaddr, "c demo connection " .. luaspot.sender)
	end,

	connection = function(neigh)
		print("neighbor: " .. luaspot.sender .. " - " .. neigh)
	end,

	who = function()
		luaspot.send_raw(luaspot.sender, "c demo add")
	end,

	add = function()
		print(luaspot.sender)
	end,

	ping = function()
		print("[who] ping src=" .. luaspot.sender)
		luaspot.send(luaspot.sender, "c demo pong")
	end,

	pong = function()
		print("ping response from " .. luaspot.sender)
	end,

	blink = function()
		for i=1,3 do
			for j=0,7 do
				sunspot.led_rgb(j, 255, 255, 0)
				sunspot.led_off(j)
			end

			sunspot.sleep(300)

			for j=0,7 do
				sunspot.led_on(j)
				sunspot.sleep(300)
			end
		end

		for j=0,7 do
			sunspot.led_rgb(j, 255, 255, 0)
			sunspot.led_off(j)
		end
	end,

}

