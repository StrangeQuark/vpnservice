FROM nginx:alpine

RUN apk add --no-cache iproute2
COPY start-router.sh /start-router.sh
RUN chmod +x /start-router.sh

USER nginx

ENTRYPOINT ["/start-router.sh"]
