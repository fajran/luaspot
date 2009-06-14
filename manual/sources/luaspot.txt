.. _luaspot:

Lua SPOT
========

Compilation
-----------

To compile Lua SPOT, go to the Lua SPOT project directory and make the
application suite by entering ``ant suite`` command as follows::

   $ cd src/LuaSpot
   $ ant suite

After executing that, under the ``suite`` directory, you can find a file
called ``LuaSpot_1.0.0.jar``. 

Deployment
----------

To install the application to the Sun SPOT, run ``ant deploy`` in the
project directory. This will compile and install the suite to the Sun
SPOT.::

   $ ant deploy

Execution
---------

After the deployment, the Lua SPOT is ready to be used. Reset the Sun SPOT
and after some time, you can see the LEDs turn to red. It tells that the
Lua SPOT is initializing. If the LEDs turn to green and off after that, it
means that the Lua SPOT in the Sun SPOT is ready to be used.


