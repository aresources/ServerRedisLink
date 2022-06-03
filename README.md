ServerRedisLink
Manage servers efficiently

This plugin is extremely simple & lightweight (just one class), but too useful. It simply adds your server to yours proxies instantly using Redis.

How does it works?
Simple, when a server is turned on (the plugin is loaded) it registers the data on Redis (server name (root server folder name), address & port). After that, uses RedisPubSub to notify all proxies to register the server. When the server is turned off (the plugin is unloaded), it disappear from proxies.
