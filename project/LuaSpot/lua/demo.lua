demo = {
	discover = function()
		luaspot.send_raw("broadcast", "c demo introduce " .. luaspot.sender)
	end,

	introduce = function(bsaddr)
		luaspot.send_raw(luaspot.pkt_src, "c demo report " .. bsaddr)
	end,

	report = function(bsaddr)
		luaspot.send_raw(bsaddr, "c demo connection " .. luaspot.pkt_src)
	end,

	connection = function(neigh)
		print(luaspot.sender, neigh)
	end,

	who = function()
		luaspot.send_raw(luaspot.sender, "c demo add")
	end,

	add = function()
		print(luaspot.sender)
	end

}

