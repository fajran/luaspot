
accelled = {
	setled = function()
		deg = sunspot.accel_x()
		deg = deg + 90
		deg = deg / 180
		deg = deg * 8

		last = luaspot.mem_get("accelled", "pos")
		if (last ~= nil) then
			sunspot.led_off(tonumber(last))
		end

		sunspot.led_rgb(deg, 255, 255, 0)
		sunspot.led_on(deg)

		luaspot.mem_set("accelled", "pos", tostring(deg))
	end
}

