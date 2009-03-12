newapp = {
	blink = function()
		for i=0,7 do
			sunspot.led_off(i)
			sunspot.led_rgb(i, 255, 0, 0)
		end
		for j=0,2 do
			for i=0,7 do
				sunspot.led_on(i)
			end
			sunspot.sleep(500)
			for i=0,7 do
				sunspot.led_off(i)
			end
			sunspot.sleep(500)
		end
	end
}

