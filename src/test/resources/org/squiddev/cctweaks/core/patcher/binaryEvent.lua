local str = '\255\244\255\233'
os.queueEvent('foobar', str)
local _, msg = os.pullEvent('foobar')
assert.assertEquals(str, msg)
