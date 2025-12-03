# TeamPvP
팀을 이루어서 일정 시간 동안 야생을 진행하며 상대방을 처치해 자신의 팀으로 흡수하는 PvP 게임 모드입니다.
## 주요 기능
- **월드 롤백 기능** - 게임 종료 후 자동으로 월드를 초기화하여 항상 새로운 환경 제공
- **팀 기능** - 팀 선택 또는 랜덤 팀 배정, 팀끼리 PvP 방지 기능 적용
- **스코어보드 기능** - 실시간 게임 상태를 표시 가능한 스코어보드
- **데스매치 기능** - 일정 시간이 지나면 데스매치 아레나로 이동
- **관전자 기능** - 게임 진행 도중에 참여한 플레이어 또는 탈락한 플레이어는 관전 모드로 전환되어 게임을 지켜 볼 수 있음
## 야생 월드 리셋 방법
서버 시작 스크립트에 다음 내용을 추가해주세요. 서버가 시작될 때 기존 야생 월드(`world`, `world_nether`, `world_the_end`)가 삭제되고 새로 생성됩니다.
```shell
#!/usr/bin/env bash

MEMORY="4G"
JAR="server.jar"

mkdir -p logs

while true; do
    if [ -d "newplugins" ]; then
        echo "Moving new plugins..."
        mv newplugins/* plugins/ 2>/dev/null
        rm -rf newplugins/*
    fi

    echo "Deleting survival worlds..."
    rm -rf world world_nether world_the_end

    java -Xms$MEMORY -Xmx$MEMORY --add-modules=jdk.incubator.vector -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1 -Dusing.aikars.flags=https://mcflags.emc.gs -Daikars.new.flags=true -jar "$JAR" --nogui

    echo "===================================="
    echo "  Lobby stopped. Restarting in 5s..."
    echo "===================================="
    sleep 5
done
```