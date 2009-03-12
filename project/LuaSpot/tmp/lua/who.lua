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
		luaspot.dispatch("pengirim", "c who echo2 een twee")
	end
}

