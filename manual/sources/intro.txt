Introduction
============

Lua SPOT is a script execution framework on `Sun SPOT
<http://www.sunspotworld.com/>`_, a wireless sensor network. It uses a
tiny Lua virtual machine that is written in Java, `Kahlua
<http://code.google.com/p/kahlua/>`_.

Lua SPOT allows you to develop applications in Lua scripting language
and run the applications on the Sun SPOT. Each application contains
one or more function that can be invoked by sending a message in a
certain format to the Sun SPOT. Therefore, we can see the Sun SPOT as
a service provider that contains services which can be executed
remotely.

This Lua SPOT package contains two applications. The first one is
:ref:`Lua SPOT <luaspot>` itself which will be installed on the Sun
SPOT. The second one is :ref:`LuaDeskSpot <luadeskspot>` which is a
desktop application that can be used to send messages to the Sun SPOT
and do a certain task that shows the service invocation.

