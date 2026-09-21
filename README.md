# VPNService
**VPNService** is an upcoming microservice for provisioning and managing WireGuard VPN devices through a secure Spring Boot API.<br><br>
It will let authorized users create, rotate, and revoke VPN device configurations while keeping WireGuard responsible for the encrypted VPN tunnel itself.
<br><br><br>

## Planned Features
- WireGuard device provisioning and revocation
- One-time WireGuard configuration download when a device is created or rotated
- PostgreSQL storage for device ownership, public keys, and assigned VPN addresses
- AuthService JWT integration for user ownership and access control
- Standalone operation when AuthService is not included in a stack
- Ready-to-run Docker and Docker Compose environment
- Postman collection for testing and exploration
  <br><br><br>

## Planned Technology Stack
- Java 21
- Spring Boot
- PostgreSQL
- WireGuard
- Docker & Docker Compose
- JPA (Hibernate)
- JUnit 5
  <br><br><br>

## Access Model
When integrated with AuthService, VPN access will be restricted by authorization:

- `VPN_API_ACCESS` — granted by default to `DEVELOPER`, `ADMIN`, and `SUPER` users
- `VPN_MANAGEMENT` — granted by default to `SUPER` users for cross-user device management

Normal `USER` accounts will not receive VPN access by default. When AuthService is not included, VPNService follows the existing MSINIT standalone pattern and does not apply API authorization.
<br><br><br>

## API
- Create a VPN device and receive a one-time WireGuard configuration
- List the current user's VPN devices
- Revoke a VPN device
- Rotate a compromised or lost device configuration
- List and revoke devices across users for VPN managers
  <br><br><br>

## Security Model
VPNService will not persist WireGuard client private keys. A private key will be generated only while creating or rotating a device, returned once in the generated configuration, and then discarded. The database will retain only the device public key, assigned VPN address, ownership information, and creation metadata.
<br><br>

The Spring Boot API will run without elevated network privileges. The separate WireGuard runtime will own the VPN interface and receive only the capabilities required to manage it.
<br><br><br>

## Getting Started
Implementation is in progress. The service will follow the standard MSINIT workflow:

```
git clone https://github.com/StrangeQuark/vpnservice.git
cd vpnservice
docker compose up --build
```
<br>

### Environment Variables
The future `.env` file will provide database credentials, encryption secrets, WireGuard connection settings, and integration flags. Do not use example credentials outside local development.
<br><br>

## API Documentation
A Postman collection is included in the root of the project:

- `VpnService.postman_collection.json`

It includes local administrator mode and AuthService integration requests.
<br><br>

## Testing
Unit tests cover VPN device provisioning and revocation. TestService end-to-end coverage will be added with the service implementation.
<br><br>

## License
This project is licensed under the Apache License 2.0. See `LICENSE` for details.
<br><br>

## Contributing
Contributions are welcome! Feel free to open issues or submit pull requests.
