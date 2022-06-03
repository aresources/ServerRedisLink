ServerRedisLink
Manage servers efficiently

This plugin is extremely simple & lightweight (just one class), but too useful. It simply adds your server to yours proxies instantly using Redis.

How does it works?
Simple, when a server is turned on (the plugin is loaded) it registers the data on Redis (server name (root server folder name), address & port). After that, uses RedisPubSub to notify all proxies to register the server. When the server is turned off (the plugin is unloaded), it disappear from proxies.

Want to register a server manually?
The idea of this plugin is to simplify how your network works, but if for any reason you don't want to have this plugin on a specific server, you can register it manually following these simple steps.

redis-cli
to enter your Redis console.

set server-link:servername serverip:serverport
(remember to replace with the corresponding data).

publish server-link update
to notify proxies.
