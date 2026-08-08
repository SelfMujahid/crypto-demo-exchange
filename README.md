# Crypto Demo Exchange

Ek demo crypto exchange Android app — Binance/Bitget/Coinbase jaisa UI aur experience, 
lekin real money ke bina. Live prices Binance ke public WebSocket (WSS) se aate hain, 
taaki data extreme fast aur real-time ho.

## Tech Stack
- **Language:** Kotlin (native Android)
- **UI:** Jetpack Compose + Material 3
- **Real-time data:** WebSocket (WSS) — Binance public stream
- **Build:** GitHub Actions (cloud build, no local Android Studio needed)

## Build Status
Har push pe GitHub Actions automatically APK build karta hai. 
APK "Actions" tab > latest run > "Artifacts" section se download hoti hai.

## Project Structure
- `app/` — main Android app module
- `.github/workflows/build.yml` — CI/CD pipeline jo APK banata hai

## Roadmap
- [x] Phase 1: Base project + CI pipeline
- [ ] Phase 2: Binance WSS se live prices
- [ ] Phase 3: Demo balance + buy/sell engine
- [ ] Phase 4: Charts + order book UI
