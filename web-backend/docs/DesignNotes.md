# Design notes
## Security
- The reason VMS login is necessary is anyone could open the web app publicly otherwise and start running attacks on open unauthenticated ports. Right now we're running on a single VM and directly exposing the port to the outside world.
- The other option is to install a VPN server on the VM and connect it on the tablet directly. Then we don't need a VMS user login. We can also avoid a TLS certificate and public DNS, but would need a VPN license.
- 2 user roles
    - Default VMS user role to login to the app and register guests
    - Internal employee admin role to manage settings and view visit reports. This will be mapped to select few users.
- User email and mobile to be stored encrypted.
