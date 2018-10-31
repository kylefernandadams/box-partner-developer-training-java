export default {
  oidc: {
    clientId: '{clientId}',
    issuer: 'https://{YOUR_OKTA_DOMAIN}.com/oauth2/default',
    redirectUri: 'http://localhost:8080/implicit/callback',
    scope: 'openid profile email',
  },
  resourceServer: {
    messagesUrl: 'http://localhost:8080/api/messages',
  },
  features: {
//    registration: true
  }
};
