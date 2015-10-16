-- Check basic FS works

local str = '\255\244\255\233'
-- Write file
local handle = fs.open('foo', 'w')
handle.write(str)
handle.close()
-- Read file
handle = fs.open('foo', 'r')
local msg = handle.readAll()
handle.close()
-- Check
assert.assertEquals(str, msg)
