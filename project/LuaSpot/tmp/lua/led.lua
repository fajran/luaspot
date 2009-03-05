
-- sunspot = {
-- 	led_on = function(pos)
-- 		print("led on:", pos)
-- 	end,
-- 	led_off = function(pos)
-- 		print("led off:", pos)
-- 	end,
-- 	led_rgb = function(pos, r, g, b)
-- 		print("led rgb:", pos, "rgb:", r, g, b)
-- 	end
-- }

prev = -1
cnt = -1
step = function()
	if (cnt == -1) then
		pos = 0
		while pos < 8 do
			sunspot.led_off(pos)
			sunspot.led_rgb(pos, 255, 255, 0)
			pos = pos + 1
		end
		cnt = cnt + 1
	end

	if prev >= 0 then
		sunspot.led_off(prev)
	end
	sunspot.led_on(cnt)

	prev = cnt
	cnt = (cnt + 1) % 8
end

-- step()
-- step()
-- step()
-- step()
-- step()
-- step()
-- step()
-- step()
-- step()
-- step()
-- step()

