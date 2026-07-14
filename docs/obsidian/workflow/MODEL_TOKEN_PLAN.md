---
aliases: [Model ve Token Plani, Codex Model Secim Plani]
type: workflow
status: current
implementation_status: ready
updated: 2026-07-14
tags: [gymtracker/workflow, gymtracker/codex, gymtracker/token]
---

# GymTracker - Model ve Token Tuketimi Plani

> [!summary]
> Ana strateji: kesif ve tekrarli islerde en ucuz model, gunluk kodlamada dengeli model, yalnizca riskli ve belirsiz kararlarda guclu model. Varsayilan olarak `xhigh`, Max, Ultra ve Fast kullanilmaz.

## Mevcut durum teshisi

Kullanici ayarlarinda su anda `gpt-5.6-sol` ve `xhigh` reasoning sabitlenmis durumda. Bu ikisi birlikte neredeyse her isi en pahali yoldan calistirir. Ayrica hesap `gpt-5.6-sol` model kimligini kabul etmedigi icin su hata olusur:

`The 'gpt-5.6-sol' model is not supported when using Codex with a ChatGPT account`

Bu nedenle model secicide gorunmeyen bir model kimligi global ayara sabitlenmemelidir. Hesapta kullanilabilen secenekler icin uygulamadaki model secici esas alinmalidir.

## Model merdiveni

| Seviye | Model / arayuz secimi | Reasoning | Ne zaman kullanilir? | Ne zaman kullanilmaz? |
|---|---|---|---|---|
| 0 - En ekonomik | Luna / Faster | Light veya Low | Markdown, Obsidian notu, adlandirma, formatlama, basit test ekleme, mekanik refactor, log ozetleme, belli dosyada kucuk UI duzeltmesi | Mimari karar, karmasik hata, Room migration, guvenlik |
| 1 - Gunluk varsayilan | Terra | Low; gerekirse Medium | Ozellik gelistirme, Compose ekranlari, ViewModel, repository, standart refactor, test duzeltme, kod inceleme | Cok belirsiz veya yuksek riskli degisiklik |
| 2 - Derin analiz | Sol veya genel `gpt-5.6` / Power | Medium veya High | Mimari tasarim, veri kaybi riski, concurrency, navigation state, zor hata ayiklama, kapsamli review | Dosya tasima, belge guncelleme, rutin kod |
| 3 - Istisna | Sol/`gpt-5.6` | Extra High, Max | Iki normal deneme basarisizsa ve sorun hala cok adimliysa | Gunluk gelistirme ve ilk deneme |
| 4 - Cok nadir | Ultra | Ultra | Birbirinden bagimsiz, gercekten paralel alt problemlere ayrilabilen buyuk calisma | Tek ekran, tek bug, tek PR, token tasarrufu hedeflenen oturum |

Hesap `gpt-5.6-sol` kimligini reddederse Seviye 2 icin model seciciden **Power** veya desteklenen genel `gpt-5.6` secilir. Bunlar da gorunmuyorsa Terra + High kullanilir; desteklenmeyen model kimligi zorlanmaz.

## GymTracker gorevlerine gore secim

| Is | Ilk secim | Yukseltme kosulu |
|---|---|---|
| [[SESSION_HANDOFF]] ve ilgili graf dugumlerini okuma | Luna Light | Notlar kodla celisiyorsa Terra Low |
| Dosya bulma, `rg`, kaynak atlasindan hedef secme | Luna Light | Bagimlilik zinciri belirsizse Terra Low |
| Kucuk Compose stil/spacing/renk degisikligi | Luna Light | Tema, erisilebilirlik ve state birlikte etkileniyorsa Terra Medium |
| Yeni standart ekran veya component | Terra Medium | Birden fazla feature/state akisina dokunuyorsa Sol Medium |
| ViewModel ve reaktif state duzeltmesi | Terra Medium | Race condition veya lifecycle/process recreation sorunu varsa Sol High |
| Room entity/DAO degisikligi | Terra Medium ile kapsam cikarma | Migration tasarimi ve veri koruma kontrolu Sol High |
| Navigation degisikligi | Terra Medium | Deep link, saved state veya karmasik back stack varsa Sol High |
| Basit unit test yazma | Luna Low | Test tasarimi davranisi netlestirmeyi gerektiriyorsa Terra Medium |
| Build/lint/test komutlarini calistirma | Luna Light | Hata nedenini bulmak icin Terra Medium |
| Derleyici hatasi, acik hata mesaji | Terra Low | Iki odakli duzeltme denemesi basarisizsa Sol High |
| Belirsiz crash veya veri tutarsizligi | Terra Medium | Birden fazla katman/lifecycle/concurrency etkileniyorsa Sol High |
| PR diff ozeti ve dokumantasyon | Luna Light | Mimari sapma veya regresyon araniyorsa Terra/Sol High |
| Guvenlik, odeme veya kullanici verisi incelemesi | Sol High | Yalniz en zor ve cok adimli durumda Extra High |

## Gelistirme asamalarinda model kullanimi

### 1. Oturum baslangici - Luna Light

1. [[SESSION_HANDOFF]] okunur.
2. Ilgili MOC ve feature dugumu acilir.
3. [[CHANGE_IMPACT_MAP]] uzerinden hedef dosyalar belirlenir.
4. Tum depo taranmaz; yalniz notun `source_files` alani ve mevcut Git diff dogrulanir.

Cikti en fazla su dort parcadan olusur: hedef, etkilenen dosyalar, riskler, dogrulama komutlari.

### 2. Kapsam ve plan - Terra Low/Medium

- Tek PR'a sigacak en kucuk dikey dilim secilir.
- Uygulama adimlari 3-7 maddeyle sinirlanir.
- Her adim icin dosya ve kabul kriteri yazilir.
- Mimari karar gerekmiyorsa Sol'a gecilmez.

### 3. Uygulama - Terra Medium

- Bir oturumda tek ozellik veya tek hata ele alinir.
- Ilk once en kucuk calisan degisiklik yapilir.
- Kod uretmeden once tum proje tekrar anlatilmaz.
- Mekanik alt isler Luna'ya birakilabilir: adlandirma, belge, basit test iskeleti, formatlama.

### 4. Dogrulama - Luna, sonra Terra

- Mevcut test/build/lint komutlarini calistirma ve basarili sonucu ozetleme: Luna Light.
- Net bir hata mesajini duzeltme: Terra Low/Medium.
- Ayni hata icin iki odakli deneme basarisizsa: Sol High.
- Tum log yerine ilk anlamli hata, ilgili stack trace ve son komut sonucu tutulur.

### 5. Risk incelemesi - ihtiyaca gore Terra veya Sol

- Standart UI/feature diff'i: Terra Medium.
- Room migration, veri kaybi, auth/odeme, concurrency, lifecycle ve cok katmanli refactor: Sol High.
- Extra High yalniz normal inceleme belirsizligi gideremediyse kullanilir.

### 6. Kapanis - Luna Light

- Etkilenen Obsidian notlari guncellenir.
- [[SESSION_HANDOFF]] icine tamamlanan is, test sonucu ve sonraki tek adim yazilir.
- Uzun sohbet ozeti yerine dosyalara kalici bilgi aktarilir.

## Otomatik yukseltme ve dusurme kurallari

### Bir ust seviyeye gec

Asagidakilerden en az biri varsa model veya reasoning bir kademe yukseltilebilir:

- Kabul kriteri veya dogru davranis belirsiz.
- Degisiklik veri kaybi ya da guvenlik riski tasiyor.
- Uc veya daha fazla katman ayni anda etkileniyor.
- Lifecycle, concurrency, migration veya karmasik navigation soz konusu.
- Ayni net sorunda iki odakli deneme basarisiz oldu.
- Testler gecse bile mimari regresyon ihtimali yuksek.

Ayni anda hem modeli hem reasoning seviyesini yukseltme. Once Terra Low -> Terra Medium, sonra gerekirse Sol/Power Medium -> High sirasi izlenir.

### Bir alt seviyeye don

- Plan ve karar tamamlandi, is mekanik uygulamaya dondu.
- Hata izole edildi ve cozum netlesti.
- Yalniz test calistirma, formatlama veya dokumantasyon kaldi.
- Kalan degisiklik tek dosya ve acik kabul kriterine sahip.

## Token tasarrufu icin oturum kurallari

1. Her task tek hedefle acilir; ilgisiz isler ayri task olur.
2. Prompt dort alan icerir: hedef, baglam, kisitlar, tamamlanma kosulu.
3. Dosya yollarini acikca ver; "tum projeyi incele" komutunu uygulama asamasinda kullanma.
4. Obsidian grafini indeks olarak kullan, fakat degisecek kaynak dosyayi ve Git diff'i dogrula.
5. Uzun kaynak dosyalardan yalniz ilgili sembol ve cevresini oku.
6. Komut ciktisini tekrar tekrar gonderme; ilk anlamli hatayi ozetle.
7. Basarisiz denemede ayni promptu tekrarlama; yeni kaniti ve degisen varsayimi yaz.
8. Gereksiz web aramasi, ekran goruntusu, subagent ve Ultra kullanma.
9. Fast mode token/credit tasarrufu saglamaz; hiz icin daha fazla kredi harcadigindan kapali tutulur.
10. "Once planla, sonra onay bekle" yalniz karar gercekten kullanici secimi gerektiriyorsa kullanilir.

## Kisa prompt sablonu

```text
Hedef: [tek ve olculebilir degisiklik]
Baglam: Once [[SESSION_HANDOFF]] ve [ilgili dugum] notunu oku. Yalniz [dosyalar] kapsaminda calis.
Kisitlar: Mevcut mimariyi koru; ilgisiz dosyalara dokunma; [ozel risk].
Tamamlanma: [test/build/lint] basarili, [davranis] dogrulanmis ve ilgili Obsidian notu guncellenmis olsun.
Token: Ilk olarak en dusuk yeterli reasoning kullan. Belirtilen yukseltme kosulu olusmadan tum depoyu tarama veya derin modele gecme.
```

## Onerilen varsayilan ayar

En dusuk tuketim odakli global ayar:

```toml
model = "gpt-5.6-terra"
model_reasoning_effort = "low"
service_tier = "default"
```

Eger hesap Terra model kimligini de kabul etmiyorsa `model` satiri kaldirilir ve uygulamadaki **Faster** secimi kullanilir. Global ayarda `xhigh` tutulmaz; zor tasklarda model seciciden gecici olarak Medium/High secilir.

CLI kullaniminda ayri profiller tercih edilebilir:

```toml
# ~/.codex/deep-review.config.toml
model = "gpt-5.6"
model_reasoning_effort = "high"
service_tier = "default"
```

Bu profil yalniz zor incelemelerde `codex --profile deep-review` ile acilir. Fast mode varsayilan yapilmaz.

## Haftalik kontrol

- Hangi tasklarda model yukseltildi?
- Yukseltme gercekten sonucu degistirdi mi?
- Ayni hata iki kez tekrarlandi mi; [[AGENTS]] veya graf notuna yeni kural eklenmeli mi?
- Uzun oturumlar daha kucuk PR'lara ayrilabilir mi?
- `xhigh`, Max veya Ultra gereksiz kullanildi mi?

Hedef dagilim:

- Islerin %50-65'i Luna/Faster + Light/Low
- Islerin %25-40'i Terra + Low/Medium
- Islerin %5-10'u Sol/Power + Medium/High
- Extra High/Max/Ultra: yalniz istisna

Bu yuzdeler faturalama garantisi degil, kullanim disiplini hedefidir.

## Resmi dayanak

- [Codex model secimi](https://learn.chatgpt.com/docs/models.md)
- [Codex yapilandirma temelleri](https://learn.chatgpt.com/docs/config-file/config-basic.md)
- [Codex gelismis yapilandirma ve profiller](https://learn.chatgpt.com/docs/config-file/config-advanced.md)
- [Codex hiz ve Fast mode](https://learn.chatgpt.com/docs/agent-configuration/speed.md)
- [Codex en iyi uygulamalar](https://learn.chatgpt.com/guides/best-practices.md)

## Iliskili dugumler

- [[00_START_HERE]]
- [[SESSION_START_PROTOCOL]]
- [[SESSION_HANDOFF]]
- [[CHANGE_IMPACT_MAP]]
- [[TEST_MATRIX]]
- [[GRAPH_MAINTENANCE]]
