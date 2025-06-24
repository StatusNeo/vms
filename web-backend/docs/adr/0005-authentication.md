# Architecture Decision Record

## Title
Integrate Azure AD OAuth2 for authentication

## Status
Accepted

## Context
The application must support secure authentication and role-based access control, ideally integrated with StatusNeo's existing O365 infrastructure.

## Decision
We will use Azure AD OAuth2 for authentication and authorization, leveraging Spring Security's OAuth2 client support.

## Consequences
- Seamless integration with StatusNeo O365 accounts
- Centralized user management and SSO
- Requires Azure AD configuration and ongoing maintenance

## Alternatives Considered
- Local authentication: More maintenance, less secure
- Google OAuth2: Not aligned with company infrastructure 