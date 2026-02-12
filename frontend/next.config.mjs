const config = {
 async rewrites() {
  return [{ source: '/api/:path*', destination: `${process.env.API_ORIGIN || 'http://localhost:8081'}/api/:path*` }];
 }
};
export default config;
