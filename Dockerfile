FROM nginx:1.21.0

COPY ./nginx-startup.sh /docker-entrypoint.d/
COPY ./nginx.conf /etc/nginx/conf.d/default.conf
COPY ./dist/demo/. /usr/share/nginx/html

RUN chgrp -R root /var/cache/nginx /var/run /var/log/nginx && \
    chmod -R 770 /var/cache/nginx /var/run /var/log/nginx

EXPOSE 8080
