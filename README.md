# AdvancedCoreSurvival-Plugin
# AdvancedCoreSurvival
Modular premium **Survival Core** untuk Paper/Spigot yang menggabungkan **Essentials**, **Economy & Shops**, **RPG/MMORPG**, **Claim**, **Party & Team**, dengan integrasi **Vault / EssentialsX / CMI / PlaceholderAPI**.  
Dikembangkan oleh **Minekarta Studio**.

---

## Ringkasan Cepat
- **Modular penuh**: setiap modul bisa `enabled/disabled` lewat config.
- **Namespace rapi**: semua PlaceholderAPI memakai awalan `%advancedcoresurvival_<modul>_<nama>%`.
- **Permission konsisten**: semua permission memakai awalan `advancedcoresurvival.<modul>.<aksi>`.
- **Integrasi luas**: Vault, EssentialsX, CMI, GriefPrevention, Lands, WorldGuard, Factions, RedProtect, PlaceholderAPI.

---

## Kompatibilitas & Dependensi
- **Server**: Paper/Spigot 1.20+ (Direkomendasikan Paper).
- **Java**: 17+.
- **Dependensi**:
  - **Wajib**: PlaceholderAPI (untuk placeholder), Vault (jika `economy.mode=vault`).
  - **Opsional**: EssentialsX, CMI, GriefPrevention, Lands, WorldGuard, Factions, RedProtect, bStats.
- **Prioritas Integrasi**: `CMI > EssentialsX > Internal` (bisa diubah di config).

---

## Instalasi
1. Letakkan `AdvancedCoreSurvival.jar` ke folder `plugins/`.
2. (Opsional) Instal plugin integrasi (Vault, EssentialsX/CMI, plugin claim).
3. Nyalakan server sekali untuk menghasilkan folder & file config.
4. Buka `/plugins/AdvancedCoreSurvival/` dan sesuaikan konfigurasi.
5. Reload plugin atau restart server untuk menerapkan perubahan.

---

## Konfigurasi Utama (modular)
```yaml
modules:
  claims: true
  essentials-integration: true
  cmi-integration: true
  survival-essentials: true
  economy: true
  auction-house: true
  player-shops: true
  admin-shops: true
  rpg: true
  mmorpg: true
  party: true
  team: true
  placeholderapi: true

integration:
  priority: "cmi,essentials,internal" # urutan fallback
  essentialsx:
    enabled: true
    override-commands: false
    use-economy: true
    use-homes: true
    use-spawn: true
  cmi:
    enabled: true
    override-commands: false
    use-economy: true
    use-homes: true
    use-spawn: true

storage:
  type: sqlite   # sqlite | mysql | yaml
  mysql:
    host: localhost
    port: 3306
    database: acs
    username: root
    password: ""
    pool-size: 10

locale: "en_US"    # tersedia: en_US, id_ID (bisa ditambah)
debug: false
```

> Jika suatu modul **disabled**, seluruh command/placeholder terkait akan **dinonaktifkan** otomatis.

---

## Modul & Konfigurasi Spesifik

### 1) 🔒 Claims
**Fitur**: Internal chunk-claim, trust, flags, integrasi GriefPrevention/Lands/WorldGuard/Factions/RedProtect.  
**Config**:
```yaml
claims:
  provider: "internal" # internal | griefprevention | lands | worldguard | factions | redprotect
  internal:
    max-claims-per-player: 3
    claim-size: 16
    tax-per-day: 0
    flags:
      pvp: false
      fire-spread: false
      mob-griefing: false
```
**Commands & Permissions**:
- `/claim create` → `advancedcoresurvival.claim.create`
- `/claim delete` → `advancedcoresurvival.claim.delete`
- `/claim trust <player>` → `advancedcoresurvival.claim.trust`
- `/claim untrust <player>` → `advancedcoresurvival.claim.untrust`
- `/claim info` → `advancedcoresurvival.claim.info`
- `/claim flags <flag> <on|off>` → `advancedcoresurvival.claim.flags`
**Placeholders**:
- `%advancedcoresurvival_claim_owner%`
- `%advancedcoresurvival_claim_size%`
- `%advancedcoresurvival_claim_world%`
**Tab-Completer**:
- Subcommand: `create|delete|trust|untrust|info|flags`
- Player: daftar pemain online
- Flag: `pvp|fire-spread|mob-griefing`
- Toggle: `on|off`

---

### 2) 🏠 Survival Essentials
**Fitur**: home/spawn/tpa/back. Bisa override EssentialsX/CMI atau pakai internal.  
**Config**:
```yaml
essentials:
  max-homes: 3
  allow-bed-home: true
  tpa:
    cooldown: 30
    expire: 60
  back:
    enabled: true
```
**Commands & Permissions**:
- `/home [name]` → `advancedcoresurvival.essentials.home`
- `/sethome <name>` → `advancedcoresurvival.essentials.sethome`
- `/delhome <name>` → `advancedcoresurvival.essentials.delhome`
- `/spawn` → `advancedcoresurvival.essentials.spawn`
- `/setspawn` → `advancedcoresurvival.essentials.setspawn`
- `/tpa <player>` → `advancedcoresurvival.essentials.tpa`
- `/tpaccept` → `advancedcoresurvival.essentials.tpaccept`
- `/tpdeny` → `advancedcoresurvival.essentials.tpdeny`
- `/back` → `advancedcoresurvival.essentials.back`
**Placeholders**:
- `%advancedcoresurvival_essentials_home_count%`
- `%advancedcoresurvival_essentials_spawn_world%`
- `%advancedcoresurvival_essentials_tpa_status%`
**Tab-Completer**:
- Nama home (autocomplete dari data player)
- Player online (untuk `/tpa`)

---

### 3) 💰 Economy & Shops
**Fitur**: Internal economy (Vault-compatible), Auction House (GUI), Player Shops (sign), Admin Shops (GUI).  
**Config**:
```yaml
economy:
  mode: internal    # internal | vault
  starting-balance: 1000
  currency-symbol: "$"
  pay:
    enabled: true
    min-amount: 1
    tax-percent: 0
  baltop:
    limit: 10

auctionhouse:
  enabled: true
  listing-fee: 50
  sales-tax: 5
  expiration: "48h"
  max-active-listings: 5

playershop:
  enabled: true
  sign-format:
    line1: "[Shop]"
    line2: "%amount%"
    line3: "%price%"
    line4: "%item%"
  allow-buy: true
  allow-sell: true
  price-limits:
    min: 1
    max: 1000000

adminshop:
  enabled: true
  categories:
    blocks:
      name: "Blocks"
      icon: "STONE"
      items:
        - item: "DIAMOND"
          price: 100
          type: "buy"       # buy | sell | buy-sell
        - item: "IRON_INGOT"
          price: 20
          type: "buy-sell"
```
**Commands & Permissions**:
- `/balance` → `advancedcoresurvival.economy.balance`
- `/pay <player> <amount>` → `advancedcoresurvival.economy.pay`
- `/baltop` → `advancedcoresurvival.economy.baltop`
- `/ah` → `advancedcoresurvival.economy.auctionhouse`
- `/ah sell <price>` → `advancedcoresurvival.economy.auctionhouse.sell`
- `/ah list [player]` → `advancedcoresurvival.economy.auctionhouse.list`
- `/ah cancel <listing-id>` → `advancedcoresurvival.economy.auctionhouse.cancel`
- `/adminshop` → `advancedcoresurvival.economy.adminshop`
**Placeholders**:
- `%advancedcoresurvival_economy_balance%`
- `%advancedcoresurvival_economy_baltop_rank%`
- `%advancedcoresurvival_economy_ah_listings%`
**Tab-Completer**:
- `/pay <online_player> <amount>`
- `/ah <sell|list|cancel>` dan listing-id milik pemain
- Kategori & item adminshop dari `adminshop.categories`

**Format PlayerShop (Sign)**:
```
[Shop]
<amount>
<price>
<item>
```
Contoh:
```
[Shop]
64
500
STONE
```

---

### 4) 🗡️ RPG
**Fitur**: Level, EXP, Skills (Mining/Farming/Fishing/Combat/Magic), Stats (Strength/Agility/Intelligence/Vitality), bonus pasif.  
**Config**:
```yaml
rpg:
  enabled: true
  exp-sources:
    mining: 2
    farming: 1
    mob-kill: 5
    fishing: 2
  skills:
    mining:
      bonus-exp: 0.1
      drop-chance: 0.05
    combat:
      damage-multiplier: 1.05
```
**Commands & Permissions**:
- `/rpg level` → `advancedcoresurvival.rpg.level`
- `/rpg stats` → `advancedcoresurvival.rpg.stats`
- `/rpg skill <name>` → `advancedcoresurvival.rpg.skill`
**Placeholders**:
- `%advancedcoresurvival_rpg_level%`
- `%advancedcoresurvival_rpg_exp%`
- `%advancedcoresurvival_rpg_skill_mining%`
- `%advancedcoresurvival_rpg_stat_strength%`
**Tab-Completer**:
- Subcommand: `level|stats|skill`
- Skill: `mining|farming|fishing|combat|magic`

---

### 5) ⚔️ MMORPG
**Fitur**: Class (Warrior/Archer/Mage/Healer), Abilities (cooldown, mana), Skill Tree.  
**Config**:
```yaml
mmorpg:
  enabled: true
  classes:
    warrior:
      base-stats: { strength: 5, vitality: 5 }
      abilities:
        - "Slash:damage=5;cooldown=5;mana=0"
    mage:
      base-stats: { intelligence: 7, mana: 100 }
      abilities:
        - "Fireball:damage=8;cooldown=10;mana=20"
  mana:
    base: 50
    regen-per-5s: 5
```
**Commands & Permissions**:
- `/class choose <name>` → `advancedcoresurvival.mmorpg.class.choose`
- `/class info` → `advancedcoresurvival.mmorpg.class.info`
- `/ability list` → `advancedcoresurvival.mmorpg.ability.list`
- `/ability use <ability>` → `advancedcoresurvival.mmorpg.ability.use`
**Placeholders**:
- `%advancedcoresurvival_mmorpg_class%`
- `%advancedcoresurvival_mmorpg_mana%`
- `%advancedcoresurvival_mmorpg_ability_cooldown_<ability>%`
**Tab-Completer**:
- `/class <choose|info>` + daftar class dari config
- `/ability <list|use>` + daftar ability aktif pemain

---

### 6) 👥 Party
**Fitur**: Kelompok kecil (maks 6), share EXP, share loot, party chat.  
**Config**:
```yaml
party:
  enabled: true
  max-size: 6
  exp-share: true
  loot-share: "round-robin"   # round-robin | free-for-all | leader-priority
```
**Commands & Permissions**:
- `/party create` → `advancedcoresurvival.party.create`
- `/party invite <player>` → `advancedcoresurvival.party.invite`
- `/party join <player>` → `advancedcoresurvival.party.join`
- `/party leave` → `advancedcoresurvival.party.leave`
- `/party kick <player>` → `advancedcoresurvival.party.kick`
- `/pchat <message>` → `advancedcoresurvival.party.chat`
**Placeholders**:
- `%advancedcoresurvival_party_name%`
- `%advancedcoresurvival_party_size%`
**Tab-Completer**:
- Subcommand: `create|invite|join|leave|kick|info`
- Player online
- `/pchat` tidak autocomplete pesan

---

### 7) 🏳️ Team
**Fitur**: Grup besar (maks 50), ranks (Leader/Officer/Member), integrasi klaim.  
**Config**:
```yaml
team:
  enabled: true
  max-size: 50
  ranks: [LEADER, OFFICER, MEMBER]
  claim-integration: true
```
**Commands & Permissions**:
- `/team create <name>` → `advancedcoresurvival.team.create`
- `/team join <name>` → `advancedcoresurvival.team.join`
- `/team leave` → `advancedcoresurvival.team.leave`
- `/team invite <player>` → `advancedcoresurvival.team.invite`
- `/team kick <player>` → `advancedcoresurvival.team.kick`
- `/team promote <player>` → `advancedcoresurvival.team.promote`
- `/team demote <player>` → `advancedcoresurvival.team.demote`
**Placeholders**:
- `%advancedcoresurvival_team_name%`
- `%advancedcoresurvival_team_rank%`
- `%advancedcoresurvival_team_size%`
**Tab-Completer**:
- Subcommand: `create|join|leave|invite|kick|promote|demote|info`
- Player online
- Nama tim (dari data)

---

## PlaceholderAPI – Referensi Lengkap
Gunakan format: `%advancedcoresurvival_<modul>_<nama>%`

- **claims**: `claim_owner`, `claim_size`, `claim_world`
- **essentials**: `home_count`, `spawn_world`, `tpa_status`
- **economy**: `balance`, `baltop_rank`, `ah_listings`
- **rpg**: `level`, `exp`, `skill_<name>`, `stat_<name>`
- **mmorpg**: `class`, `mana`, `ability_cooldown_<ability>`
- **party**: `party_name`, `party_size`
- **team**: `team_name`, `team_rank`, `team_size`

Jika modul nonaktif → placeholder mengembalikan string kosong `""` atau `"disabled"` (configurable).

---

## Integrasi & Prioritas
- **Vault**: jembatan ekonomi (EssentialsX/CMI/other).  
- **EssentialsX**/**CMI**: homes/spawn/chat/economy; jika keduanya hadir → default **CMI** (ubah via `integration.priority`).  
- **Claim Plugins**: otomatis terdeteksi; jika tak ada → fallback ke **internal claim**.  
- **PlaceholderAPI**: semua modul mendaftarkan expansion masing‑masing.  

---

## Data, Kinerja, Keamanan
- **Penyimpanan**: SQLite/MySQL/YAML; operasi I/O berat berjalan **async**.
- **Caching**: balance, level, class, party/team state disimpan di memory dengan sinkronisasi periodik.
- **Anti-exploit**: cek duplikasi item untuk Auction/PlayerShop, limit harga, konfirmasi transaksi, cooldown ability/TPA.
- **bStats**: opsional; nonaktifkan via config.
- **Update checker**: opsional; hanya notifikasi ke console/op.

---

## Internationalization (i18n)
- Folder: `/plugins/AdvancedCoreSurvival/languages/`
- File contoh: `messages_en_US.yml`, `messages_id_ID.yml`
- Semua pesan bisa diubah; mendukung MiniMessage/Legacy color codes.

---

## Struktur Paket (Saran)
```
com.minekarta.advancedcoresurvival
 ┣ core (bootstrap, config, command, scheduler)
 ┣ modules
 ┃ ┣ claims
 ┃ ┣ essentials
 ┃ ┣ economy
 ┃ ┃ ┣ auctionhouse
 ┃ ┃ ┣ playershops
 ┃ ┃ ┗ adminshops
 ┃ ┣ rpg
 ┃ ┣ mmorpg
 ┃ ┣ party
 ┃ ┗ team
 ┣ integrations
 ┃ ┣ vault
 ┃ ┣ essentialsx
 ┃ ┣ cmi
 ┃ ┗ landplugins (gp, lands, wg, factions, redprotect)
 ┣ placeholders (expansions per modul)
 ┗ api (events, provider interfaces)
```

---

## API untuk Developer
**Event (contoh)**:
- `ACSBalanceChangeEvent(player, old, new, reason)`
- `ACSSkillLevelUpEvent(player, skill, oldLvl, newLvl)`
- `ACSClassChangeEvent(player, oldClass, newClass)`
- `ACSPartyJoinEvent(player, party)`
- `ACSTeamRankChangeEvent(player, oldRank, newRank)`
- `ACSClaimCreateEvent(player, region)`
- `ACSAuctionCreateEvent(player, listing)`
- `ACSShopPurchaseEvent(player, item, price)`

**Provider Interfaces**:
- `EconomyProvider` (getBalance, deposit, withdraw, transfer)
- `ClaimProvider` (isInClaim, canBuild, getOwner)
- `ChatProvider` (optional; relay ke Essentials/CMI jika perlu)

**Placeholder Registration**: satu expansion per modul dengan prefix `advancedcoresurvival_<modul>`.

---

## `plugin.yml` (Draft)
```yaml
name: AdvancedCoreSurvival
main: com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival
version: 1.0.0
api-version: "1.21"
author: Minekarta Studio
softdepend:
  - Vault
  - PlaceholderAPI
  - Essentials
  - CMI
  - GriefPrevention
  - Lands
  - WorldGuard
  - Factions
  - RedProtect
commands:
  advancedcoresurvival:
    description: Main admin command
    permission: advancedcoresurvival.admin
    usage: /advancedcoresurvival <reload|debug|about>
  home:
    permission: advancedcoresurvival.essentials.home
  sethome:
    permission: advancedcoresurvival.essentials.sethome
  delhome:
    permission: advancedcoresurvival.essentials.delhome
  spawn:
    permission: advancedcoresurvival.essentials.spawn
  setspawn:
    permission: advancedcoresurvival.essentials.setspawn
  tpa:
    permission: advancedcoresurvival.essentials.tpa
  tpaccept:
    permission: advancedcoresurvival.essentials.tpaccept
  tpdeny:
    permission: advancedcoresurvival.essentials.tpdeny
  back:
    permission: advancedcoresurvival.essentials.back
  balance:
    permission: advancedcoresurvival.economy.balance
  pay:
    permission: advancedcoresurvival.economy.pay
  baltop:
    permission: advancedcoresurvival.economy.baltop
  ah:
    permission: advancedcoresurvival.economy.auctionhouse
  adminshop:
    permission: advancedcoresurvival.economy.adminshop
  rpg:
    permission: advancedcoresurvival.rpg.base
  class:
    permission: advancedcoresurvival.mmorpg.base
  ability:
    permission: advancedcoresurvival.mmorpg.ability.base
  party:
    permission: advancedcoresurvival.party.base
  pchat:
    permission: advancedcoresurvival.party.chat
  team:
    permission: advancedcoresurvival.team.base
permissions:
  advancedcoresurvival.*:
    default: op
    children:
      advancedcoresurvival.admin: true
      advancedcoresurvival.essentials.*: true
      advancedcoresurvival.economy.*: true
      advancedcoresurvival.rpg.*: true
      advancedcoresurvival.mmorpg.*: true
      advancedcoresurvival.party.*: true
      advancedcoresurvival.team.*: true
      advancedcoresurvival.claim.*: true
  advancedcoresurvival.admin:
    default: op
  advancedcoresurvival.essentials.*:
    default: true
    children:
      advancedcoresurvival.essentials.home: true
      advancedcoresurvival.essentials.sethome: true
      advancedcoresurvival.essentials.delhome: true
      advancedcoresurvival.essentials.spawn: true
      advancedcoresurvival.essentials.setspawn: op
      advancedcoresurvival.essentials.tpa: true
      advancedcoresurvival.essentials.tpaccept: true
      advancedcoresurvival.essentials.tpdeny: true
      advancedcoresurvival.essentials.back: true
  advancedcoresurvival.economy.*:
    default: true
    children:
      advancedcoresurvival.economy.balance: true
      advancedcoresurvival.economy.pay: true
      advancedcoresurvival.economy.baltop: true
      advancedcoresurvival.economy.auctionhouse: true
      advancedcoresurvival.economy.auctionhouse.sell: true
      advancedcoresurvival.economy.auctionhouse.list: true
      advancedcoresurvival.economy.auctionhouse.cancel: true
      advancedcoresurvival.economy.adminshop: true
  advancedcoresurvival.claim.*:
    default: true
    children:
      advancedcoresurvival.claim.create: true
      advancedcoresurvival.claim.delete: true
      advancedcoresurvival.claim.trust: true
      advancedcoresurvival.claim.untrust: true
      advancedcoresurvival.claim.info: true
      advancedcoresurvival.claim.flags: true
  advancedcoresurvival.rpg.*:
    default: true
    children:
      advancedcoresurvival.rpg.base: true
      advancedcoresurvival.rpg.level: true
      advancedcoresurvival.rpg.stats: true
      advancedcoresurvival.rpg.skill: true
  advancedcoresurvival.mmorpg.*:
    default: true
    children:
      advancedcoresurvival.mmorpg.base: true
      advancedcoresurvival.mmorpg.class.choose: true
      advancedcoresurvival.mmorpg.class.info: true
      advancedcoresurvival.mmorpg.ability.base: true
      advancedcoresurvival.mmorpg.ability.list: true
      advancedcoresurvival.mmorpg.ability.use: true
  advancedcoresurvival.party.*:
    default: true
    children:
      advancedcoresurvival.party.base: true
      advancedcoresurvival.party.create: true
      advancedcoresurvival.party.invite: true
      advancedcoresurvival.party.join: true
      advancedcoresurvival.party.leave: true
      advancedcoresurvival.party.kick: true
      advancedcoresurvival.party.chat: true
  advancedcoresurvival.team.*:
    default: true
    children:
      advancedcoresurvival.team.base: true
      advancedcoresurvival.team.create: true
      advancedcoresurvival.team.join: true
      advancedcoresurvival.team.leave: true
      advancedcoresurvival.team.invite: true
      advancedcoresurvival.team.kick: true
      advancedcoresurvival.team.promote: true
      advancedcoresurvival.team.demote: true
```

---

## Tab Completer – Prinsip Umum
- **Subcommand-first**: selalu suguhkan daftar subcommand yang valid.
- **Dynamic suggestions**: player online, daftar home, nama tim/party, daftar class/ability, listing-id.
- **Config-driven**: class/ability/kategori shop diambil dari config saat runtime.
- **Fail-safe**: jika modul dinonaktifkan, completer mengembalikan daftar kosong.

---

## Roadmap
- Custom survival items (grappling hook, miner’s compass).
- Reputation-based shops (integrasi AdvancedPlayerContract).
- Scoreboard & tablist untuk statistik survival/RPG.
- Cross-server sync (database) untuk economy/claim/party/team.
- Web panel sederhana untuk admin (opsional).

---

## Dukungan & Laporan Masalah
- Gunakan issue tracker repositori Anda (GitHub/BBBit). Sertakan versi server, log error, dan langkah reproduksi.
- Mohon lampirkan file config terkait saat melaporkan bug.

---

**Minekarta Studio** — Terima kasih telah menggunakan AdvancedCoreSurvival!
