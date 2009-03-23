.. _scripting:

Scripting Reference
===================

Once the Lua SPOT is ready and deployed, you can add new applications to it
by writing a Lua script. The script script is need to be compiled before
you can actually send it to the Lua SPOT.


Lua Installation
----------------

Lua 5.1.4 is needed to compile the Lua scripts that are going to be
installed on the Lua SPOT. The Lua source code can be downloaded from
http://www.lua.org/ftp/lua-5.1.4.tar.gz. Follow the installation
instruction inside the source code archive to install Lua on your machine.


Writing Lua Script for Lua SPOT
-------------------------------

Before writing the script, you have to determine a name for your script.
This name will also be your script identifier when installing and calling
the script from the Lua SPOT.

The script should be written according to the following standard.

.. code-block:: lua
   :linenos:

   appname = {
      func1 = function()
         -- your code here
      end,
      
      func2 = function()
         -- your other code here
      end,
      
      -- your other functions
   }

Your application name will be a new table containing all functions that you
want to make. 

Functions can have one or more parameters. Lua SPOT will only pass one
parameter, therefore if you expect more parameters, you have to split the
parameters manually. There is a function provided by the Lua SPOT
(``luaspot.next_token``) that will help you split the parameters.

Lua SPOT will only send the parameter using string format. You have to
manually convert the string to number or other format you expect if you
don't want to use string. For example, you can use ``tonumber()`` function
to convert a string to a number.


Compiling The Script
--------------------

After writing the script, you have to compile the script into the binary
format that later can be installed to the Lua SPOT.

Use the following command to compile the script.::

   $ luac -o app.lbc app.lua

Substitue ``app.lua`` with your script name and ``app.lbc`` will be the
binary format of your script.


Installing The Script
---------------------

The binary version of the script can be installed to the Sun SPOT (with Lua SPOT installed) using the help of LuaDeskSpot application. Follow the :ref:`instruction <install>` that has been explained earlier.


Lua SPOT APIs
-------------

In order to use facilities that are provided by Sun SPOT and Lua SPOT,
there are several functions that can be used by the Lua script. These functions will be divided into two components, Sun SPOT and Lua SPOT API.


Sun SPOT API
^^^^^^^^^^^^

This API provides functions to access functions provided by Sun SPOT.

#. ``sunspot.led_on(pos)``

   Turns on the LED at position ``pos``.

#. ``sunspot.led_off(pos)``

   Turns off the LED at position ``pos``.

#. ``sunspot.led_rgb(pos, r, g, b)``

   Sets the color of LED at position ``pos`` with RGB value of ``(r, g, b)``.

#. ``sunspot.temp_celcius()``

   Returns temperature value in Celcius.

#. ``sunspot.temp_fahrenheit()``

   Returns temperature value in Fahrenheit.

#. ``sunspot.accel_x()``

   Returns the accelerometer value at x axis.

#. ``sunspot.accel_y()``

   Returns the accelerometer value at y axis.

#. ``sunspot.accel_z()``

   Returns the accelerometer value at z axis.

#. ``sunspot.sleep(delay)``

   Pauses the execution for ``delay`` miliseconds.


Lua SPOT API
^^^^^^^^^^^^

This API provides functions to access functions provided by Lua SPOT.

#. ``luaspot.send(addr, msg)``

   Sends a message ``msg`` to destination address ``addr`` using the
   routing function.

#. ``luaspot.send_raw(addr, msg)``

   Sends a message ``msg`` to destination address ``addr`` directly without
   using the routing function.

#. ``luaspot.add_id(id)``

   This function is used to insert a new message id provided in ``id`` to
   the message id table. This table is intended to store all received
   message id so messages with duplicate id can be dropped.

#. ``luaspot.check_id(id)``

   Checks whether a message id is already marked as received or not.

#. ``luaspot.get_new_id()``

   Returns a new globally unique message id.

#. ``luaspot.get_node_addr()``

   Returns the node address.

#. ``luaspot.mem_set(app, var, value)``

   Stores a value to the persistent storage. The value ``value`` is
   identified by application name ``app`` and a variable name ``var``.

#. ``luaspot.mem_get(app, var)``

   Returns a value from the persistent storage. The value is located using
   the application name ``app`` and a variable name ``var``.

#. ``luaspot.dispatch(addr, msg)``

   Dispatches a new message ``msg`` from source address ``addr`` to the
   main Lua SPOT message dispatcher.

#. ``luaspot.lock(app)``

   Creates a global lock identified by ``app``. Other threads that are
   trying to acquire lock with the same identifier will get blocked until
   the corresponding ``luaspot.unlock`` function called.

#. ``luaspot.unlock(app)``

   Releases the lock identified by ``app``.

#. ``luaspot.next_token(str)``

   Gets the next token inside the string ``str``. This function returns two values, the next token and the remaining string.

