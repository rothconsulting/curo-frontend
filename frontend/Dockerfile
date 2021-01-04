from nginx:1.19.6

COPY ./nginx-startup.sh /docker-entrypoint.d/
COPY ./nginx.conf /etc/nginx/conf.d/default.conf
COPY ./dist/demo/. /usr/share/nginx/html
