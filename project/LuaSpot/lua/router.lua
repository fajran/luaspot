router = {
	route = function(param)
		print("[router] receive data=" .. param)
		id,p = luaspot.next_token(param)
		print("[router] id=" .. id)

		if luaspot.check_id(id) then
			print("[router] duplicate id=" .. id)
			return
		end

		luaspot.add_id(id)
	
		hop,p = luaspot.next_token(p)
		hop = tonumber(hop)
		print("[router] hop=" .. hop)

		src,p = luaspot.next_token(p)
		dst,msg = luaspot.next_token(p)
		print("[router] src=" .. src)
		print("[router] dst=" .. dst)
		print("[router] msg=" .. msg)

		if dst == luaspot.get_node_addr() or dst == "broadcast" then
			luaspot.sender = src
			luaspot.dispatch(src, msg)
			print("[router] process message id=" .. id .." src=" .. src)
		end
	
		hop = hop - 1
		if hop > 0 and dst ~= luaspot.get_node_addr() then
			luaspot.send_raw("broadcast", "c router route " .. id .. " " .. hop .. " " .. src .. " " .. dst .. " " .. msg)
			print("[router] forward packet id=" .. id .. " src=" .. src .. " dst=" .. dst)
		end


	end
}
