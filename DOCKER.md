# 🚀 How to Run (Docker)

## 1. Build Docker Image
```bash
docker build -t sapue-server-tcp:latest .
```

##
2. Run Container
```bash
docker run --network host sapue-server-tcp:latest
```