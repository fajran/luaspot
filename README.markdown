LuaSpot
=======

Lua SPOT is a script execution framework on [Sun SPOT](http://www.sunspotworld.com/), 
a wireless sensor network. It uses [Kahlua](http://code.google.com/p/kahlua/), 
a tiny [Lua](http://www.lua.org/) virtual machine that is written in Java.

Lua SPOT allows you to develop applications in Lua scripting language and run
the applications on the Sun SPOT. Each application contains one or more
function that can be invoked by sending a message in a certain format to the
Sun SPOT. Therefore, we can see the Sun SPOT as a service provider that
contains services which can be executed remotely.

This Lua SPOT package contains two applications. The first one is Lua SPOT
itself which will be installed on the Sun SPOT. The second one is LuaDeskSpot
which is a desktop application that can be used to send messages to the Sun
SPOT and do a certain task that shows the service invocation.

This work was done as part of [Network Programming](http://www.science.uva.nl/research/sne/) 
course at the [Universiteit van Amsterdam](http://www.uva.nl). The work was supervised by 
[Rudolf Strijkers](http://staff.science.uva.nl/~rjstrijk/).


