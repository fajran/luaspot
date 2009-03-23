who = {
	echo1 = function(param)
		print("[echo1] param:", param)
		a,b = luaspot.next_token(param)
		print("[echo1] a:", a)
	end,

	echo2 = function(param)
		print("[echo2] param:", param)
		a,b = luaspot.next_token(param)
		print("[echo2] a:", a)
		print("[echo2] b:", b)
	end,

	addr = function()
		print("paket source:", luaspot.pkt_src)
	end,

	dispatch = function()
		luaspot.dispatch(luaspot.sender, "c who blink")
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

	ping = function()
		print("[who] ping src=" .. luaspot.sender)
		luaspot.send(luaspot.sender, "c who pong")
	end,

	pong = function()
		print("ping response from " .. luaspot.sender)
	end
}

