function assert(v, oldmessage)
	if oldmessage == nil then
		message="assertion failed!"
	else
		message = oldmessage;
	end

	if not v then
		error(message)
	end
	
	return v, oldmessage;
end