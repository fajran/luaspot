router = {
	route = function(param)
		id,p = luaspot.next_token(param)

		if luaspot.check_id(id) then
			return
		end

		luaspot.add_id(id)
	
		hop,p = luaspot.next_token(p)
		hop = tonumber(hop)

		src,p = luaspot.next_token(p)
		dst,msg = luaspot.next_token(p)

		hop = hop - 1
		if hop > 0 and dst ~= luaspot.get_node_addr() then
			luaspot.send_raw("broadcast", "c router route " .. id .. " " .. hop .. " " .. src .. " " .. dst .. " " .. msg)
		end

		if dst == luaspot.get_node_addr() or dst == "broadcast" then
			luaspot.sender = src
			luaspot.dispatch(luaspot.pkt_src, msg)
		end

	end
}
