# AnarchyCore Plugin

## Customization

Modify the `tab_header` and `tab_footer` variables to personalize your server's appearance. These variables are located in the main class. If you're unsure how to proceed, feel free to reach out to me directly at admin@deluxemc.net, and I'll guide you through the process!

## Compatibility

### Why doesn't the plugin work on Paper, Spigot, or CraftBukkit?

While Paper is a fork of Spigot, which in turn is a fork of CraftBukkit, our plugin specifically targets Folia. This is because we utilize regionalized threading, which differs significantly from the default Bukkit scheduler, ensuring better performance and stability under Folia. In the future, we plan to add an option to switch between the Bukkit scheduler and Folia's scheduler.