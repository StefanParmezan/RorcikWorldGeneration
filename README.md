<div align="center">

<img src="https://capsule-render.vercel.app/api?type=waving&height=160&color=0:36BCF7,50:6C63FF,100:FF4F9A&text=Rorcik%20World%20Generation&fontSize=42&fontColor=ffffff&fontAlignY=38&desc=Worldgen%20overhaul%20for%20Minecraft%201.20.1&descAlignY=60&descSize=16&animation=fadeIn" alt="Rorcik World Generation" width="100%" />

![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-3C8527?style=for-the-badge)
![Forge](https://img.shields.io/badge/Forge-47.4.10-E04E14?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Mixins](https://img.shields.io/badge/Mixins-8A2BE2?style=for-the-badge)
![TerraBlender](https://img.shields.io/badge/TerraBlender-3.0.0-6DB33F?style=for-the-badge)
![RorcikCore](https://img.shields.io/badge/RorcikCore-1.0.0-6C63FF?style=for-the-badge)
![License](https://img.shields.io/badge/License-All_Rights_Reserved-B22222?style=for-the-badge)

</div>

## О проекте

**Rorcik World Generation (RWG)** — worldgen-мод для Minecraft 1.20.1, который перерабатывает генерацию мира: климатические зоны, биомы, рельеф, руды, растительность и интеграцию с системой городов.

Мод является частью экосистемы Rorcik и зависит от **RorcikCore**.

## Возможности

- **Климатическое зонирование.** Кастомная density-функция `ConcentricTemperature` задаёт концентрические температурные пояса, по которым распределяются биомы.
- **Кастомные биомы.** Регистрация и подключение биомов через TerraBlender.
- **Mixins в генерацию мира.** Изменение генерации чанков, биомов, placed features, структур и руд.
- **Рельеф городов.** `CityPlateauFunction` выравнивает высоту вокруг городских зон и плавно смешивает её с естественным рельефом.
- **Кастомные фичи.** Валуны, поваленные деревья, дикие фермы, земляные пятна, прибрежная растительность, сглаживание берегов и плит.
- **Исправление воды и биомов.** Водные колонки в сухопутных биомах превращаются в реки/океаны, а сухие участки водных биомов — в сушу.
- **Гибкая конфигурация.** Пещеры, руды, растительность, биомные зоны, совместимость с LSO и физика блоков настраиваются через common-конфиг Forge.
- **Интеграция с городами.** Кэш и глобальный реестр городских зон используются для согласования worldgen и процедурной генерации структур.

## Стек

| Компонент | Версия |
|:---|:---|
| Minecraft | 1.20.1 |
| Forge | 47.4.10 |
| Java | 17 |
| TerraBlender | 1.20.1-3.0.0.164 |
| RorcikCore | 1.0.0 |
| Mixins | SpongePowered Mixin |

## Зависимости

Обязательные:

- **RorcikCore** — общие зоны, реестры и API;
- **TerraBlender** — регистрация и подключение биомов.

Опциональные:

- **DynamicTreesPlus** — дополнительная совместимость;
- **Legendary Survival Overhaul (LSO)** — отключение конфликтующих растений через конфиг;
- **MineCollapse** — физика блоков для указанных в конфиге блоков.

## Структура проекта

```text
src/main/java/ru/rorcik/rwg/
├── block/              # Слабы, кусты, кластеры и другие блоки
├── command/            # Отладочные команды
├── mixin/              # Mixins в генерацию мира
├── world/
│   ├── biomemodifier/  # Модификаторы биомов и фич
│   ├── city/           # Кэш и реестр городских зон
│   ├── density/        # Density-функции рельефа и температуры
│   ├── feature/        # Кастомные worldgen-фичи
│   └── RwgTerrablender.java
├── RwgBiomes.java
├── RwgBlocks.java
├── RwgConfig.java
└── RwgMod.java
```

Конфигурация mixins находится в `src/main/resources/rwg.mixins.json`.

## Сборка и запуск

Требования:

- JDK 17;
- Forge 1.20.1;
- Gradle Wrapper из репозитория;
- репозиторий `RorcikCore` рядом с проектом.

`RorcikWorldGeneration` подключает ядро через composite build:

```groovy
includeBuild('../RorcikCore')
```

Ожидаемая структура папок:

```text
RorcikCore/
RorcikWorldGeneration/
```

Команды:

```bash
./gradlew build
./gradlew runClient
./gradlew runServer
./gradlew runData
```

После первого запуска common-конфиг создаётся в `config/rwg-common.toml`.

## Тесты

Автотестов пока нет. Изменения проверяются запуском клиента и сервера, а также через отладочные команды и worldgen-сценарии.

## Лицензия

Код мода распространяется на условиях **All Rights Reserved** (см. `gradle.properties`).
В `LICENSE.txt` находятся лицензионные уведомления Minecraft Forge и MCP, относящиеся к MDK.

## Авторы

- Rorcik
- [StefanParmezan](https://github.com/StefanParmezan)

Вопросы и предложения — Telegram: [@StefanParmezan](https://t.me/StefanParmezan).
