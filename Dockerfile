FROM nginxinc/nginx-unprivileged:1.21.1

COPY ./nginx-startup.sh /docker-entrypoint.d/
COPY ./nginx.conf /etc/nginx/conf.d/default.conf
COPY ./dist/demo/. /usr/share/nginx/html
