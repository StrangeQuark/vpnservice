FROM python:3.13-alpine

RUN apk add --no-cache wireguard-tools

WORKDIR /wireguard-control
COPY control.py .

EXPOSE 51821
ENTRYPOINT ["python", "control.py"]
