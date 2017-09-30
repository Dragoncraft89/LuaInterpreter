function fibonacci(n)
	if n == 0 or n == 1 then
		return 1;
	end
	return fibonacci(n - 1) + fibonacci (n - 2);
end

function err()
	return "oh snap";
end

function range(n)
	t = {};
	i = 0;
	while i < n do
		i = i + 1;
		t[i] = i;
	end
	return t;
end

print("Hello world, from " .. _VERSION .. "!\n")

local i = 0;
local fib = fibonacci(i);
while true do
	print(i .. ": " .. fib)
	print(i)
	i = i + 1;
	fib = fibonacci(i);
	if fib < 1000000 then
		continue;
	else
		break;
	end
end

print(assert(dofile()));

function a()
	error("fuck this fucking shit");
end

function err()
	print("oh snap, something went wrong");
end


print(tonumber("dff", 16));

xpcall(a, err);
a();
