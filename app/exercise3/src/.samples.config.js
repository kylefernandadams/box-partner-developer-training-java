export default {
  oidc: {
    clientId: '0oagmn05oeeKRiyTR0h7',
    issuer: 'https://dev-740529.oktapreview.com/oauth2/default',
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
