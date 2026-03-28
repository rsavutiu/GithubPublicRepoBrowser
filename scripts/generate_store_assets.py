#!/usr/bin/env python3
"""
Generate Play Store screenshots and metadata for ALL 85 supported locales.

Creates framed screenshots with translated captions and generates
title.txt, short_description.txt, full_description.txt for each locale.

Usage: python scripts/generate_store_assets.py
"""

import os
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
import arabic_reshaper
from bidi.algorithm import get_display

ROOT = Path(__file__).resolve().parent.parent
SCREENSHOTS_DIR = ROOT / "screenshots"
FASTLANE_DIR = ROOT / "fastlane" / "metadata" / "android"

CAPTION_HEIGHT = 280
FINAL_WIDTH = 1080
FINAL_HEIGHT = 2400
BG_COLOR = (76, 175, 80)
TEXT_COLOR = (255, 255, 255)

# ── ALL 85 Play Console locales with translations ─────────────────────────────
# Each entry: { title, short_desc, full_desc, captions: {file: caption} }
# short_desc MUST be <= 80 bytes UTF-8

SCREENSHOTS = [
    "trending_this_week.png",
    "trending_this_year.png",
    "topic_picker.png",
    "topic_filter.png",
    "repo_detail.png",
]

# Caption keys (indexed by screenshot filename)
C_WEEK = "trending_this_week.png"
C_YEAR = "trending_this_year.png"
C_PICK = "topic_picker.png"
C_FILT = "topic_filter.png"
C_DETAIL = "repo_detail.png"

APP_TITLE = "GitHub Trending Explorer"  # 30 char limit, keep as-is for most

def _full(intro, heading, *pairs_and_outro):
    """Build full description. Args: intro, heading, then pairs of (name, desc), last arg is outro."""
    parts = [f"{intro}\n\n<b>{heading}</b>\n"]
    items = list(pairs_and_outro)
    outro = items.pop()
    for i in range(0, len(items), 2):
        parts.append(f"\n* <b>{items[i]}</b> -- {items[i+1]}\n")
    parts.append(f"\n{outro}")
    return "".join(parts)

# Master translations for all 85 locales
TRANSLATIONS = {
    "af": {
        "title": APP_TITLE,
        "short_desc": "Ontdek gewilde GitHub-repos met slim soek en onderwerpfilters.",
        "captions": {C_WEEK: "Gewild hierdie week", C_YEAR: "Beste repos van die jaar", C_PICK: "Filter volgens onderwerp", C_FILT: "Slim onderwerpfilters", C_DETAIL: "Gedetailleerde insigte"},
        "full_desc": _full("GitHub Trending Explorer is die vinnigste manier om gewilde projekte op GitHub te ontdek.", "Sleutelkenmerke:", "Gewilde ontdekking", "Blaai deur gewilde repos daagliks, weekliks, maandeliks en jaarliks.", "Slim soek", "Volteks soek via GraphQL API.", "Onderwerpfilters", "Filter volgens verskeie onderwerpe met 30+ kategoriee.", "Diep insigte", "README, bydraers, afhanklikheidsanalise.", "GitHub-verifikasie", "Meld aan om repos te ster.", "Material 3-ontwerp", "Pragtige, moderne koppelvlak.", "Geen advertensies. Geen opsporing. Oopbron."),
    },
    "am": {
        "title": APP_TITLE,
        "short_desc": "በ GitHub ላይ ታዋቂ ማከማቻዎችን በብልጥ ፍለጋ ያግኙ።",
        "captions": {C_WEEK: "በዚህ ሳምንት ታዋቂ", C_YEAR: "የዓመቱ ምርጥ", C_PICK: "በርዕስ አጣራ", C_FILT: "ብልጥ ማጣሪያዎች", C_DETAIL: "ዝርዝር መረጃ"},
        "full_desc": None,
    },
    "ar": {
        "title": "مستكشف GitHub الرائج",
        "short_desc": "اكتشف المستودعات الرائجة على GitHub مع بحث ذكي وفلاتر.",
        "captions": {C_WEEK: "الرائج هذا الأسبوع", C_YEAR: "أفضل المستودعات هذا العام", C_PICK: "تصفية حسب الموضوع", C_FILT: "فلاتر مواضيع ذكية", C_DETAIL: "تفاصيل معمّقة"},
        "full_desc": _full("مستكشف GitHub الرائج هو أسرع طريقة لاكتشاف ما هو رائج على GitHub.", "الميزات الرئيسية:", "اكتشاف الرائج", "تصفح المستودعات الرائجة يومياً وأسبوعياً وشهرياً وسنوياً.", "بحث ذكي", "بحث نصي كامل مدعوم بواجهة GraphQL.", "فلاتر المواضيع", "تصفية حسب مواضيع متعددة مع أكثر من 30 فئة.", "تفاصيل معمّقة", "عرض README والمساهمين وتحليل التبعيات.", "مصادقة GitHub", "سجّل الدخول لتمييز المستودعات بنجمة.", "تصميم Material 3", "واجهة حديثة وجميلة.", "بدون إعلانات. بدون تتبع. مفتوح المصدر."),
    },
    "az-AZ": {
        "title": APP_TITLE,
        "short_desc": "GitHub-da trend repozitoriyalari agilli axtaris ile kesfet.",
        "captions": {C_WEEK: "Bu hefte trend", C_YEAR: "Ilin en yaxsi repolari", C_PICK: "Movzuya gore filter", C_FILT: "Agilli movzu filtrleri", C_DETAIL: "Etrafli melumat"},
        "full_desc": None,
    },
    "be": {
        "title": APP_TITLE,
        "short_desc": "Адкрыйце папулярныя рэпазіторыі GitHub з разумным пошукам.",
        "captions": {C_WEEK: "Папулярнае на гэтым тыдні", C_YEAR: "Лепшыя рэпа за год", C_PICK: "Фільтр па тэме", C_FILT: "Разумныя фільтры тэм", C_DETAIL: "Падрабязная аналітыка"},
        "full_desc": None,
    },
    "bg": {
        "title": APP_TITLE,
        "short_desc": "Открийте популярни GitHub хранилища с интелигентно търсене.",
        "captions": {C_WEEK: "Популярни тази седмица", C_YEAR: "Най-добрите за годината", C_PICK: "Филтър по тема", C_FILT: "Интелигентни филтри", C_DETAIL: "Подробни данни"},
        "full_desc": None,
    },
    "bn-BD": {
        "title": APP_TITLE,
        "short_desc": "স্মার্ট সার্চ দিয়ে GitHub-এ ট্রেন্ডিং রেপো আবিষ্কার করুন।",
        "captions": {C_WEEK: "এই সপ্তাহে ট্রেন্ডিং", C_YEAR: "বছরের সেরা রেপো", C_PICK: "বিষয় অনুযায়ী ফিল্টার", C_FILT: "স্মার্ট টপিক ফিল্টার", C_DETAIL: "বিস্তারিত তথ্য"},
        "full_desc": None,
    },
    "ca": {
        "title": APP_TITLE,
        "short_desc": "Descobreix repos GitHub populars amb cerca intel-ligent i filtres.",
        "captions": {C_WEEK: "Popular aquesta setmana", C_YEAR: "Millors repos de l'any", C_PICK: "Filtrar per tema", C_FILT: "Filtres de temes intel-ligents", C_DETAIL: "Informacio detallada"},
        "full_desc": None,
    },
    "cs-CZ": {
        "title": APP_TITLE,
        "short_desc": "Objevte populární GitHub repozitare s chytrym vyhledavanim.",
        "captions": {C_WEEK: "Populární tento týden", C_YEAR: "Nejlepší repozitáře roku", C_PICK: "Filtrovat podle tématu", C_FILT: "Chytré filtry témat", C_DETAIL: "Detailní přehledy"},
        "full_desc": _full("GitHub Trending Explorer je nejrychlejší způsob, jak objevit populární projekty na GitHubu.", "Hlavní funkce:", "Objevování trendů", "Procházejte populární repozitáře denně, týdně, měsíčně a ročně.", "Chytré vyhledávání", "Fulltextové vyhledávání přes GraphQL API.", "Filtry témat", "Filtrujte podle více témat současně s 30+ kategoriemi.", "Detailní přehledy", "README, přispěvatelé, analýza závislostí.", "GitHub autentizace", "Přihlaste se a označte repozitáře hvězdičkou.", "Material 3 design", "Krásné moderní rozhraní.", "Bez reklam. Bez sledování. Open source."),
    },
    "da-DK": {
        "title": APP_TITLE,
        "short_desc": "Opdag populaere GitHub-repos med smart soegning og emnefiltre.",
        "captions": {C_WEEK: "Populært denne uge", C_YEAR: "Årets bedste repos", C_PICK: "Filtrer efter emne", C_FILT: "Smarte emnefiltre", C_DETAIL: "Dybdegående indsigt"},
        "full_desc": _full("GitHub Trending Explorer er den hurtigste måde at opdage trending projekter på GitHub.", "Hovedfunktioner:", "Trending", "Gennemse populære repos dagligt, ugentligt, månedligt og årligt.", "Smart søgning", "Fuldtekstsøgning via GraphQL API.", "Emnefiltre", "Filtrer efter flere emner med 30+ kategorier.", "Dybdegående indsigt", "README, bidragydere, afhængighedsanalyse.", "GitHub-godkendelse", "Log ind for at stjerne repos.", "Material 3-design", "Smukt, moderne interface.", "Ingen reklamer. Ingen sporing. Open source."),
    },
    "de-DE": {
        "title": APP_TITLE,
        "short_desc": "Entdecke angesagte GitHub-Repos mit intelligenter Suche.",
        "captions": {C_WEEK: "Trending diese Woche", C_YEAR: "Beste Repos des Jahres", C_PICK: "Nach Thema filtern", C_FILT: "Intelligente Themenfilter", C_DETAIL: "Detaillierte Einblicke"},
        "full_desc": _full("GitHub Trending Explorer ist der schnellste Weg, um angesagte Projekte auf GitHub zu entdecken.", "Hauptfunktionen:", "Trends entdecken", "Durchsuche beliebte Repos täglich, wöchentlich, monatlich und jährlich.", "Intelligente Suche", "Volltextsuche über die GraphQL-API.", "Themenfilter", "Filtere nach mehreren Themen mit über 30 Kategorien.", "Detaillierte Einblicke", "README, Mitwirkende, Abhängigkeitsanalyse.", "GitHub-Authentifizierung", "Melde dich an, um Repos zu markieren.", "Material 3-Design", "Schöne, moderne Oberfläche.", "Keine Werbung. Kein Tracking. Open Source."),
    },
    "el-GR": {
        "title": APP_TITLE,
        "short_desc": "Ανακαλύψτε δημοφιλή GitHub repos με έξυπνη αναζήτηση.",
        "captions": {C_WEEK: "Δημοφιλή αυτή την εβδομάδα", C_YEAR: "Κορυφαία repos του έτους", C_PICK: "Φίλτρο ανά θέμα", C_FILT: "Έξυπνα φίλτρα θεμάτων", C_DETAIL: "Λεπτομερείς πληροφορίες"},
        "full_desc": None,
    },
    "en-AU": {
        "title": APP_TITLE,
        "short_desc": "Discover trending GitHub repos with smart search, topic filters & insights.",
        "captions": {C_WEEK: "Trending This Week", C_YEAR: "Top Repos of the Year", C_PICK: "Filter by Topic", C_FILT: "Smart Topic Filters", C_DETAIL: "Deep Repo Insights"},
        "full_desc": None,  # falls back to en-US
    },
    "en-CA": {
        "title": APP_TITLE,
        "short_desc": "Discover trending GitHub repos with smart search, topic filters & insights.",
        "captions": {C_WEEK: "Trending This Week", C_YEAR: "Top Repos of the Year", C_PICK: "Filter by Topic", C_FILT: "Smart Topic Filters", C_DETAIL: "Deep Repo Insights"},
        "full_desc": None,
    },
    "en-GB": {
        "title": APP_TITLE,
        "short_desc": "Discover trending GitHub repos with smart search, topic filters & insights.",
        "captions": {C_WEEK: "Trending This Week", C_YEAR: "Top Repos of the Year", C_PICK: "Filter by Topic", C_FILT: "Smart Topic Filters", C_DETAIL: "Deep Repo Insights"},
        "full_desc": None,
    },
    "en-IN": {
        "title": APP_TITLE,
        "short_desc": "Discover trending GitHub repos with smart search, topic filters & insights.",
        "captions": {C_WEEK: "Trending This Week", C_YEAR: "Top Repos of the Year", C_PICK: "Filter by Topic", C_FILT: "Smart Topic Filters", C_DETAIL: "Deep Repo Insights"},
        "full_desc": None,
    },
    "en-SG": {
        "title": APP_TITLE,
        "short_desc": "Discover trending GitHub repos with smart search, topic filters & insights.",
        "captions": {C_WEEK: "Trending This Week", C_YEAR: "Top Repos of the Year", C_PICK: "Filter by Topic", C_FILT: "Smart Topic Filters", C_DETAIL: "Deep Repo Insights"},
        "full_desc": None,
    },
    "en-US": {
        "title": APP_TITLE,
        "short_desc": "Discover trending GitHub repos with smart search, topic filters & insights.",
        "captions": {C_WEEK: "Trending This Week", C_YEAR: "Top Repos of the Year", C_PICK: "Filter by Topic", C_FILT: "Smart Topic Filters", C_DETAIL: "Deep Repo Insights"},
        "full_desc": None,  # already exists
    },
    "en-ZA": {
        "title": APP_TITLE,
        "short_desc": "Discover trending GitHub repos with smart search, topic filters & insights.",
        "captions": {C_WEEK: "Trending This Week", C_YEAR: "Top Repos of the Year", C_PICK: "Filter by Topic", C_FILT: "Smart Topic Filters", C_DETAIL: "Deep Repo Insights"},
        "full_desc": None,
    },
    "es-419": {
        "title": APP_TITLE,
        "short_desc": "Descubre repos GitHub en tendencia con busqueda inteligente.",
        "captions": {C_WEEK: "Tendencia esta semana", C_YEAR: "Mejores repos del ano", C_PICK: "Filtrar por tema", C_FILT: "Filtros inteligentes", C_DETAIL: "Informacion detallada"},
        "full_desc": None,
    },
    "es-ES": {
        "title": APP_TITLE,
        "short_desc": "Descubre repos GitHub en tendencia con busqueda inteligente.",
        "captions": {C_WEEK: "Tendencia esta semana", C_YEAR: "Mejores repos del año", C_PICK: "Filtrar por tema", C_FILT: "Filtros de temas inteligentes", C_DETAIL: "Información detallada"},
        "full_desc": _full("GitHub Trending Explorer es la forma más rápida de descubrir proyectos en tendencia en GitHub.", "Características principales:", "Descubre tendencias", "Explora repos populares diaria, semanal, mensual y anualmente.", "Búsqueda inteligente", "Búsqueda de texto completo a través de la API GraphQL.", "Filtros de temas", "Filtra por múltiples temas con más de 30 categorías.", "Información detallada", "README, colaboradores, análisis de dependencias.", "Autenticación GitHub", "Inicia sesión para marcar repos con estrella.", "Diseño Material 3", "Interfaz moderna y hermosa.", "Sin anuncios. Sin rastreo. Código abierto."),
    },
    "es-US": {
        "title": APP_TITLE,
        "short_desc": "Descubre repos GitHub en tendencia con busqueda inteligente.",
        "captions": {C_WEEK: "Tendencia esta semana", C_YEAR: "Mejores repos del ano", C_PICK: "Filtrar por tema", C_FILT: "Filtros inteligentes", C_DETAIL: "Informacion detallada"},
        "full_desc": None,
    },
    "et": {
        "title": APP_TITLE,
        "short_desc": "Avasta populaarsed GitHub repos nutika otsingu ja filtritega.",
        "captions": {C_WEEK: "Populaarne sel nadalal", C_YEAR: "Aasta parimad repod", C_PICK: "Filtreeri teema jargi", C_FILT: "Nutikad teemafiltrid", C_DETAIL: "Uksikasjalik ulevaade"},
        "full_desc": None,
    },
    "eu-ES": {
        "title": APP_TITLE,
        "short_desc": "Aurkitu GitHub repo popularrak bilaketa adimentsuarekin.",
        "captions": {C_WEEK: "Joera aste honetan", C_YEAR: "Urteko repo onenak", C_PICK: "Iragazi gaiaren arabera", C_FILT: "Gai-iragazki adimendunak", C_DETAIL: "Xehetasun sakona"},
        "full_desc": None,
    },
    "fa": {
        "title": "مرورگر ترندهای GitHub",
        "short_desc": "مخازن محبوب GitHub را با جستجوی هوشمند کشف کنید.",
        "captions": {C_WEEK: "محبوب این هفته", C_YEAR: "بهترین مخازن سال", C_PICK: "فیلتر بر اساس موضوع", C_FILT: "فیلترهای هوشمند", C_DETAIL: "اطلاعات دقیق"},
        "full_desc": None,
    },
    "fi-FI": {
        "title": APP_TITLE,
        "short_desc": "Loyda suositut GitHub-repot haulla ja aihesuodattimilla.",
        "captions": {C_WEEK: "Suositut tällä viikolla", C_YEAR: "Vuoden parhaat repot", C_PICK: "Suodata aiheen mukaan", C_FILT: "Älykkäät aihesuodattimet", C_DETAIL: "Syvälliset näkymät"},
        "full_desc": _full("GitHub Trending Explorer on nopein tapa löytää suosittuja projekteja GitHubissa.", "Pääominaisuudet:", "Trendien löytäminen", "Selaa suosittuja repoja päivittäin, viikoittain, kuukausittain ja vuosittain.", "Älykäs haku", "Kokotekstihaku GraphQL API:n kautta.", "Aihesuodattimet", "Suodata useilla aiheilla yli 30 kategoriasta.", "Syvälliset näkymät", "README, osallistujat, riippuvuusanalyysi.", "GitHub-todennus", "Kirjaudu sisään tähdittääksesi repoja.", "Material 3 -muotoilu", "Kaunis, moderni käyttöliittymä.", "Ei mainoksia. Ei seurantaa. Avoin lähdekoodi."),
    },
    "fil": {
        "title": APP_TITLE,
        "short_desc": "Tuklasin ang trending GitHub repos gamit ang smart search.",
        "captions": {C_WEEK: "Trending ngayong linggo", C_YEAR: "Pinakamahusay ngayong taon", C_PICK: "I-filter ayon sa paksa", C_FILT: "Mga smart na filter", C_DETAIL: "Detalyadong impormasyon"},
        "full_desc": None,
    },
    "fr-CA": {
        "title": APP_TITLE,
        "short_desc": "Decouvrez les depots GitHub tendance avec recherche intelligente.",
        "captions": {C_WEEK: "Tendances cette semaine", C_YEAR: "Meilleurs depots de l'annee", C_PICK: "Filtrer par sujet", C_FILT: "Filtres intelligents", C_DETAIL: "Apercus detailles"},
        "full_desc": None,
    },
    "fr-FR": {
        "title": APP_TITLE,
        "short_desc": "Decouvrez les depots GitHub tendance avec recherche intelligente.",
        "captions": {C_WEEK: "Tendances cette semaine", C_YEAR: "Meilleurs depots de l'annee", C_PICK: "Filtrer par sujet", C_FILT: "Filtres de sujets intelligents", C_DETAIL: "Apercus detailles"},
        "full_desc": _full("GitHub Trending Explorer est le moyen le plus rapide de decouvrir les projets tendance sur GitHub.", "Fonctionnalites principales:", "Decouverte des tendances", "Parcourez les depots populaires par jour, semaine, mois et annee.", "Recherche intelligente", "Recherche plein texte via l'API GraphQL.", "Filtres de sujets", "Filtrez par plusieurs sujets avec plus de 30 categories.", "Apercus detailles", "README, contributeurs, analyse des dependances.", "Authentification GitHub", "Connectez-vous pour mettre en favori.", "Design Material 3", "Interface moderne et elegante.", "Sans publicites. Sans suivi. Open source."),
    },
    "gl-ES": {
        "title": APP_TITLE,
        "short_desc": "Descubre repos GitHub populares con busca intelixente e filtros.",
        "captions": {C_WEEK: "Popular esta semana", C_YEAR: "Mellores repos do ano", C_PICK: "Filtrar por tema", C_FILT: "Filtros intelixentes", C_DETAIL: "Informacion detallada"},
        "full_desc": None,
    },
    "gu": {
        "title": APP_TITLE,
        "short_desc": "GitHub trending repos smart search ane filters sathe shodho.",
        "captions": {C_WEEK: "Aa aathvadiye trending", C_YEAR: "Varsh na shreshth repos", C_PICK: "Vishay pramane filter", C_FILT: "Smart topic filters", C_DETAIL: "Vistrit mahiti"},
        "full_desc": None,
    },
    "hi-IN": {
        "title": APP_TITLE,
        "short_desc": "GitHub पर ट्रेंडिंग रेपो खोजें, स्मार्ट सर्च और फ़िल्टर।",
        "captions": {C_WEEK: "इस सप्ताह ट्रेंडिंग", C_YEAR: "साल के सर्वश्रेष्ठ रेपो", C_PICK: "विषय के अनुसार फ़िल्टर", C_FILT: "स्मार्ट टॉपिक फ़िल्टर", C_DETAIL: "गहन जानकारी"},
        "full_desc": _full("GitHub Trending Explorer, GitHub पर ट्रेंडिंग प्रोजेक्ट खोजने का सबसे तेज़ तरीका है।", "मुख्य विशेषताएँ:", "ट्रेंड खोजें", "दैनिक, साप्ताहिक, मासिक और वार्षिक लोकप्रिय रेपो ब्राउज़ करें।", "स्मार्ट सर्च", "GraphQL API द्वारा फुल-टेक्स्ट सर्च।", "टॉपिक फ़िल्टर", "30+ श्रेणियों में कई विषयों से फ़िल्टर करें।", "गहन जानकारी", "README, योगदानकर्ता, डिपेंडेंसी विश्लेषण।", "GitHub प्रमाणीकरण", "लॉग इन करके रेपो को स्टार करें।", "Material 3 डिज़ाइन", "सुंदर, आधुनिक इंटरफ़ेस।", "बिना विज्ञापन। बिना ट्रैकिंग। ओपन सोर्स।"),
    },
    "hr": {
        "title": APP_TITLE,
        "short_desc": "Otkrijte popularne GitHub repozitorije s pametnim pretrazivanjem.",
        "captions": {C_WEEK: "Popularno ovaj tjedan", C_YEAR: "Najbolji repozitoriji godine", C_PICK: "Filtriraj po temi", C_FILT: "Pametni filtri tema", C_DETAIL: "Detaljni uvidi"},
        "full_desc": None,
    },
    "hu-HU": {
        "title": APP_TITLE,
        "short_desc": "Fedezd fel a nepszeru GitHub repokat okos keresessel.",
        "captions": {C_WEEK: "Népszerű ezen a héten", C_YEAR: "Az év legjobb repói", C_PICK: "Szűrés téma szerint", C_FILT: "Okos témaszűrők", C_DETAIL: "Részletes betekintés"},
        "full_desc": _full("A GitHub Trending Explorer a leggyorsabb módja a népszerű GitHub projektek felfedezésének.", "Fő funkciók:", "Trendek felfedezése", "Böngészd a népszerű repókat naponta, hetente, havonta és évente.", "Okos keresés", "Teljes szöveges keresés GraphQL API-n keresztül.", "Témaszűrők", "Szűrj több téma szerint 30+ kategóriából.", "Részletes betekintés", "README, közreműködők, függőségelemzés.", "GitHub hitelesítés", "Jelentkezz be repók csillagozásához.", "Material 3 dizájn", "Gyönyörű, modern felület.", "Hirdetések nélkül. Követés nélkül. Nyílt forráskódú."),
    },
    "hy-AM": {
        "title": APP_TITLE,
        "short_desc": "Discover trending GitHub repos with smart search and filters.",
        "captions": {C_WEEK: "Trending This Week", C_YEAR: "Top Repos of the Year", C_PICK: "Filter by Topic", C_FILT: "Smart Topic Filters", C_DETAIL: "Deep Repo Insights"},
        "full_desc": None,
    },
    "id": {
        "title": APP_TITLE,
        "short_desc": "Temukan repo GitHub trending dengan pencarian cerdas dan filter.",
        "captions": {C_WEEK: "Trending Minggu Ini", C_YEAR: "Repo Terbaik Tahun Ini", C_PICK: "Filter berdasarkan Topik", C_FILT: "Filter Topik Cerdas", C_DETAIL: "Wawasan Mendalam"},
        "full_desc": _full("GitHub Trending Explorer adalah cara tercepat untuk menemukan proyek trending di GitHub.", "Fitur Utama:", "Temukan Trending", "Jelajahi repo populer harian, mingguan, bulanan, dan tahunan.", "Pencarian Cerdas", "Pencarian teks lengkap melalui GraphQL API.", "Filter Topik", "Filter berdasarkan beberapa topik dengan 30+ kategori.", "Wawasan Mendalam", "README, kontributor, analisis dependensi.", "Autentikasi GitHub", "Masuk untuk memberi bintang pada repo.", "Desain Material 3", "Antarmuka modern yang indah.", "Tanpa iklan. Tanpa pelacakan. Open source."),
    },
    "is-IS": {
        "title": APP_TITLE,
        "short_desc": "Uppgotvadu vinsael GitHub repos med snjallri leit og sium.",
        "captions": {C_WEEK: "Vinsaelt i viku", C_YEAR: "Bestu repos arsins", C_PICK: "Sia eftir efni", C_FILT: "Snjoll efnissia", C_DETAIL: "Itarlegar upplysingar"},
        "full_desc": None,
    },
    "it-IT": {
        "title": APP_TITLE,
        "short_desc": "Scopri i repo GitHub di tendenza con ricerca e filtri.",
        "captions": {C_WEEK: "Di tendenza questa settimana", C_YEAR: "I migliori repo dell'anno", C_PICK: "Filtra per argomento", C_FILT: "Filtri argomento intelligenti", C_DETAIL: "Approfondimenti dettagliati"},
        "full_desc": _full("GitHub Trending Explorer e il modo piu veloce per scoprire i progetti di tendenza su GitHub.", "Funzionalita principali:", "Scopri le tendenze", "Esplora i repo popolari giornalmente, settimanalmente, mensilmente e annualmente.", "Ricerca intelligente", "Ricerca full-text tramite API GraphQL.", "Filtri per argomento", "Filtra per piu argomenti con oltre 30 categorie.", "Approfondimenti dettagliati", "README, contributori, analisi delle dipendenze.", "Autenticazione GitHub", "Accedi per aggiungere stelle ai repo.", "Design Material 3", "Interfaccia moderna e bellissima.", "Senza pubblicita. Senza tracciamento. Open source."),
    },
    "iw-IL": {
        "title": APP_TITLE,
        "short_desc": "גלו מאגרי GitHub פופולריים עם חיפוש חכם וסינון.",
        "captions": {C_WEEK: "פופולרי השבוע", C_YEAR: "המאגרים הטובים של השנה", C_PICK: "סינון לפי נושא", C_FILT: "סינון נושאים חכם", C_DETAIL: "תובנות מעמיקות"},
        "full_desc": _full("GitHub Trending Explorer הוא הדרך המהירה ביותר לגלות פרויקטים פופולריים ב-GitHub.", "תכונות עיקריות:", "גילוי מגמות", "דפדפו במאגרים פופולריים יומית, שבועית, חודשית ושנתית.", "חיפוש חכם", "חיפוש טקסט מלא דרך GraphQL API.", "סינון נושאים", "סננו לפי מספר נושאים עם 30+ קטגוריות.", "תובנות מעמיקות", "README, תורמים, ניתוח תלויות.", "אימות GitHub", "התחברו כדי לסמן מאגרים בכוכב.", "עיצוב Material 3", "ממשק מודרני ויפה.", "ללא פרסומות. ללא מעקב. קוד פתוח."),
    },
    "ja-JP": {
        "title": APP_TITLE,
        "short_desc": "トレンドのGitHubリポをスマート検索とフィルターで発見。",
        "captions": {C_WEEK: "今週のトレンド", C_YEAR: "年間ベストリポジトリ", C_PICK: "トピックでフィルター", C_FILT: "スマートトピックフィルター", C_DETAIL: "詳細インサイト"},
        "full_desc": _full("GitHub Trending Explorerは、GitHubのトレンドプロジェクトを発見する最速の方法です。", "主な機能：", "トレンド発見", "日次、週次、月次、年次で人気リポジトリを閲覧。", "スマート検索", "GraphQL APIによるフルテキスト検索。", "トピックフィルター", "30以上のカテゴリで複数トピックをフィルター。", "詳細インサイト", "README、コントリビューター、依存関係分析。", "GitHub認証", "ログインしてリポにスターを付ける。", "Material 3デザイン", "美しくモダンなUI。", "広告なし。トラッキングなし。オープンソース。"),
    },
    "ka-GE": {
        "title": APP_TITLE,
        "short_desc": "GitHub-is popularuli repozitoriebi smart dziebis saSualebiT.",
        "captions": {C_WEEK: "Trending This Week", C_YEAR: "Top Repos of the Year", C_PICK: "Filter by Topic", C_FILT: "Smart Topic Filters", C_DETAIL: "Deep Repo Insights"},
        "full_desc": None,
    },
    "kk": {
        "title": APP_TITLE,
        "short_desc": "GitHub trending repos smart search and filters.",
        "captions": {C_WEEK: "Trending This Week", C_YEAR: "Top Repos of the Year", C_PICK: "Filter by Topic", C_FILT: "Smart Topic Filters", C_DETAIL: "Deep Repo Insights"},
        "full_desc": None,
    },
    "km-KH": {
        "title": APP_TITLE,
        "short_desc": "Discover trending GitHub repos with smart search and filters.",
        "captions": {C_WEEK: "Trending This Week", C_YEAR: "Top Repos of the Year", C_PICK: "Filter by Topic", C_FILT: "Smart Topic Filters", C_DETAIL: "Deep Repo Insights"},
        "full_desc": None,
    },
    "kn-IN": {
        "title": APP_TITLE,
        "short_desc": "GitHub trending repos smart search and topic filters.",
        "captions": {C_WEEK: "Trending This Week", C_YEAR: "Top Repos of the Year", C_PICK: "Filter by Topic", C_FILT: "Smart Topic Filters", C_DETAIL: "Deep Repo Insights"},
        "full_desc": None,
    },
    "ko-KR": {
        "title": APP_TITLE,
        "short_desc": "트렌딩 GitHub 저장소를 스마트 검색과 필터로 발견하세요.",
        "captions": {C_WEEK: "이번 주 트렌딩", C_YEAR: "올해 최고의 저장소", C_PICK: "토픽별 필터", C_FILT: "스마트 토픽 필터", C_DETAIL: "심층 인사이트"},
        "full_desc": _full("GitHub Trending Explorer는 GitHub에서 트렌딩 프로젝트를 발견하는 가장 빠른 방법입니다.", "주요 기능:", "트렌드 발견", "일간, 주간, 월간, 연간 인기 저장소를 탐색하세요.", "스마트 검색", "GraphQL API를 통한 전체 텍스트 검색.", "토픽 필터", "30개 이상의 카테고리에서 여러 토픽으로 필터링.", "심층 인사이트", "README, 기여자, 종속성 분석.", "GitHub 인증", "로그인하여 저장소에 스타를 표시.", "Material 3 디자인", "아름답고 현대적인 UI.", "광고 없음. 추적 없음. 오픈 소스."),
    },
    "ky-KG": {
        "title": APP_TITLE,
        "short_desc": "Discover trending GitHub repos with smart search and filters.",
        "captions": {C_WEEK: "Trending This Week", C_YEAR: "Top Repos of the Year", C_PICK: "Filter by Topic", C_FILT: "Smart Topic Filters", C_DETAIL: "Deep Repo Insights"},
        "full_desc": None,
    },
    "lo-LA": {
        "title": APP_TITLE,
        "short_desc": "Discover trending GitHub repos with smart search and filters.",
        "captions": {C_WEEK: "Trending This Week", C_YEAR: "Top Repos of the Year", C_PICK: "Filter by Topic", C_FILT: "Smart Topic Filters", C_DETAIL: "Deep Repo Insights"},
        "full_desc": None,
    },
    "lt": {
        "title": APP_TITLE,
        "short_desc": "Atraskite populiarias GitHub saugyklas su ismaniaja paieska.",
        "captions": {C_WEEK: "Populiaru sia savaite", C_YEAR: "Geriausios metu saugyklos", C_PICK: "Filtruoti pagal tema", C_FILT: "Ismanieji filtrai", C_DETAIL: "Issamios izvalgos"},
        "full_desc": None,
    },
    "lv": {
        "title": APP_TITLE,
        "short_desc": "Atklajiet popularus GitHub repos ar gudru meklesanu.",
        "captions": {C_WEEK: "Populari so nedelu", C_YEAR: "Gada labakais repos", C_PICK: "Filtret pec temas", C_FILT: "Gudri tematu filtri", C_DETAIL: "Detalizeta informacija"},
        "full_desc": None,
    },
    "mk-MK": {
        "title": APP_TITLE,
        "short_desc": "Откријте популарни GitHub репозитории со паметно пребарување.",
        "captions": {C_WEEK: "Популарно оваа недела", C_YEAR: "Најдобри репо за годината", C_PICK: "Филтер по тема", C_FILT: "Паметни филтри", C_DETAIL: "Детални информации"},
        "full_desc": None,
    },
    "ml-IN": {
        "title": APP_TITLE,
        "short_desc": "GitHub trending repos smart search and topic filters.",
        "captions": {C_WEEK: "Trending This Week", C_YEAR: "Top Repos of the Year", C_PICK: "Filter by Topic", C_FILT: "Smart Topic Filters", C_DETAIL: "Deep Repo Insights"},
        "full_desc": None,
    },
    "mn-MN": {
        "title": APP_TITLE,
        "short_desc": "Discover trending GitHub repos with smart search and filters.",
        "captions": {C_WEEK: "Trending This Week", C_YEAR: "Top Repos of the Year", C_PICK: "Filter by Topic", C_FILT: "Smart Topic Filters", C_DETAIL: "Deep Repo Insights"},
        "full_desc": None,
    },
    "mr-IN": {
        "title": APP_TITLE,
        "short_desc": "GitHub trending repos smart search and topic filters.",
        "captions": {C_WEEK: "Trending This Week", C_YEAR: "Top Repos of the Year", C_PICK: "Filter by Topic", C_FILT: "Smart Topic Filters", C_DETAIL: "Deep Repo Insights"},
        "full_desc": None,
    },
    "ms": {
        "title": APP_TITLE,
        "short_desc": "Temui repo GitHub trending dengan carian pintar dan penapis.",
        "captions": {C_WEEK: "Trending minggu ini", C_YEAR: "Repo terbaik tahun ini", C_PICK: "Tapis mengikut topik", C_FILT: "Penapis topik pintar", C_DETAIL: "Maklumat terperinci"},
        "full_desc": None,
    },
    "ms-MY": {
        "title": APP_TITLE,
        "short_desc": "Temui repo GitHub trending dengan carian pintar dan penapis.",
        "captions": {C_WEEK: "Trending minggu ini", C_YEAR: "Repo terbaik tahun ini", C_PICK: "Tapis mengikut topik", C_FILT: "Penapis topik pintar", C_DETAIL: "Maklumat terperinci"},
        "full_desc": None,
    },
    "my-MM": {
        "title": APP_TITLE,
        "short_desc": "Discover trending GitHub repos with smart search and filters.",
        "captions": {C_WEEK: "Trending This Week", C_YEAR: "Top Repos of the Year", C_PICK: "Filter by Topic", C_FILT: "Smart Topic Filters", C_DETAIL: "Deep Repo Insights"},
        "full_desc": None,
    },
    "nb-NO": {
        "title": APP_TITLE,
        "short_desc": "Oppdag populaere GitHub-repoer med smart soek og emnefiltre.",
        "captions": {C_WEEK: "Populaert denne uken", C_YEAR: "Aarets beste repoer", C_PICK: "Filtrer etter emne", C_FILT: "Smarte emnefiltre", C_DETAIL: "Dyptgaaende innsikt"},
        "full_desc": None,
    },
    "ne-NP": {
        "title": APP_TITLE,
        "short_desc": "GitHub trending repos smart search and topic filters.",
        "captions": {C_WEEK: "Trending This Week", C_YEAR: "Top Repos of the Year", C_PICK: "Filter by Topic", C_FILT: "Smart Topic Filters", C_DETAIL: "Deep Repo Insights"},
        "full_desc": None,
    },
    "nl-NL": {
        "title": APP_TITLE,
        "short_desc": "Ontdek trending GitHub-repos met slim zoeken en onderwerpfilters.",
        "captions": {C_WEEK: "Trending deze week", C_YEAR: "Beste repos van het jaar", C_PICK: "Filteren op onderwerp", C_FILT: "Slimme onderwerpfilters", C_DETAIL: "Diepgaande inzichten"},
        "full_desc": _full("GitHub Trending Explorer is de snelste manier om trending projecten op GitHub te ontdekken.", "Belangrijkste functies:", "Trending ontdekken", "Bekijk populaire repos per dag, week, maand en jaar.", "Slim zoeken", "Volledige tekstzoekopdracht via GraphQL API.", "Onderwerpfilters", "Filter op meerdere onderwerpen met 30+ categorieen.", "Diepgaande inzichten", "README, bijdragers, afhankelijkheidsanalyse.", "GitHub-authenticatie", "Log in om repos te sterren.", "Material 3-design", "Mooi, modern interface.", "Geen advertenties. Geen tracking. Open source."),
    },
    "no-NO": {
        "title": APP_TITLE,
        "short_desc": "Oppdag populaere GitHub-repoer med smart soek og emnefiltre.",
        "captions": {C_WEEK: "Populaert denne uken", C_YEAR: "Aarets beste repoer", C_PICK: "Filtrer etter emne", C_FILT: "Smarte emnefiltre", C_DETAIL: "Dyptgaaende innsikt"},
        "full_desc": _full("GitHub Trending Explorer er den raskeste maaten aa oppdage populaere prosjekter paa GitHub.", "Hovedfunksjoner:", "Oppdage trender", "Bla gjennom populaere repoer daglig, ukentlig, maanedlig og aarlig.", "Smart soek", "Fulltekstsoek via GraphQL API.", "Emnefiltre", "Filtrer etter flere emner med 30+ kategorier.", "Dyptgaaende innsikt", "README, bidragsytere, avhengighetsanalyse.", "GitHub-autentisering", "Logg inn for aa stjerne repoer.", "Material 3-design", "Vakkert, moderne grensesnitt.", "Ingen annonser. Ingen sporing. Aapen kildekode."),
    },
    "pa": {
        "title": APP_TITLE,
        "short_desc": "GitHub trending repos smart search and topic filters.",
        "captions": {C_WEEK: "Trending This Week", C_YEAR: "Top Repos of the Year", C_PICK: "Filter by Topic", C_FILT: "Smart Topic Filters", C_DETAIL: "Deep Repo Insights"},
        "full_desc": None,
    },
    "pl-PL": {
        "title": APP_TITLE,
        "short_desc": "Odkryj popularne repozytoria GitHub z inteligentnym wyszukiwaniem.",
        "captions": {C_WEEK: "Popularne w tym tygodniu", C_YEAR: "Najlepsze repozytoria roku", C_PICK: "Filtruj wg tematu", C_FILT: "Inteligentne filtry tematów", C_DETAIL: "Szczegółowe informacje"},
        "full_desc": _full("GitHub Trending Explorer to najszybszy sposób na odkrywanie popularnych projektów na GitHubie.", "Główne funkcje:", "Odkrywanie trendów", "Przeglądaj popularne repozytoria dziennie, tygodniowo, miesięcznie i rocznie.", "Inteligentne wyszukiwanie", "Wyszukiwanie pełnotekstowe przez GraphQL API.", "Filtry tematów", "Filtruj według wielu tematów z ponad 30 kategoriami.", "Szczegółowe informacje", "README, współtwórcy, analiza zależności.", "Uwierzytelnianie GitHub", "Zaloguj się, aby oznaczać repozytoria gwiazdką.", "Design Material 3", "Piękny, nowoczesny interfejs.", "Bez reklam. Bez śledzenia. Open source."),
    },
    "pt-BR": {
        "title": APP_TITLE,
        "short_desc": "Descubra repos GitHub em alta com busca inteligente e filtros.",
        "captions": {C_WEEK: "Em alta esta semana", C_YEAR: "Melhores repos do ano", C_PICK: "Filtrar por topico", C_FILT: "Filtros inteligentes de topicos", C_DETAIL: "Insights detalhados"},
        "full_desc": _full("GitHub Trending Explorer e a maneira mais rapida de descobrir projetos em alta no GitHub.", "Principais recursos:", "Descubra tendencias", "Navegue por repos populares diariamente, semanalmente, mensalmente e anualmente.", "Busca inteligente", "Busca de texto completo via API GraphQL.", "Filtros de topicos", "Filtre por multiplos topicos com mais de 30 categorias.", "Insights detalhados", "README, contribuidores, analise de dependencias.", "Autenticacao GitHub", "Faca login para dar estrela em repos.", "Design Material 3", "Interface moderna e bonita.", "Sem anuncios. Sem rastreamento. Codigo aberto."),
    },
    "pt-PT": {
        "title": APP_TITLE,
        "short_desc": "Descubra repos GitHub em alta com pesquisa inteligente e filtros.",
        "captions": {C_WEEK: "Em alta esta semana", C_YEAR: "Melhores repos do ano", C_PICK: "Filtrar por topico", C_FILT: "Filtros inteligentes", C_DETAIL: "Informacoes detalhadas"},
        "full_desc": None,
    },
    "ro": {
        "title": APP_TITLE,
        "short_desc": "Descopera depozitele GitHub in tendinte cu cautare inteligenta.",
        "captions": {C_WEEK: "In tendinte saptamana aceasta", C_YEAR: "Cele mai bune depozite ale anului", C_PICK: "Filtreaza dupa subiect", C_FILT: "Filtre inteligente de subiecte", C_DETAIL: "Informatii detaliate"},
        "full_desc": _full("GitHub Trending Explorer este cel mai rapid mod de a descoperi proiectele populare pe GitHub.", "Caracteristici principale:", "Descopera tendinte", "Rasfoieste depozitele populare zilnic, saptamanal, lunar si anual.", "Cautare inteligenta", "Cautare text integral prin API-ul GraphQL.", "Filtre de subiecte", "Filtreaza dupa mai multe subiecte cu peste 30 de categorii.", "Informatii detaliate", "README, contributori, analiza dependentelor.", "Autentificare GitHub", "Conecteaza-te pentru a marca cu stea.", "Design Material 3", "Interfata moderna si frumoasa.", "Fara reclame. Fara urmarire. Open source."),
    },
    "ru-RU": {
        "title": APP_TITLE,
        "short_desc": "Откройте популярные репозитории GitHub с умным поиском.",
        "captions": {C_WEEK: "В тренде на этой неделе", C_YEAR: "Лучшие репозитории года", C_PICK: "Фильтр по теме", C_FILT: "Умные фильтры тем", C_DETAIL: "Подробная аналитика"},
        "full_desc": _full("GitHub Trending Explorer -- самый быстрый способ находить популярные проекты на GitHub.", "Основные функции:", "Обзор трендов", "Просматривайте популярные репозитории за день, неделю, месяц и год.", "Умный поиск", "Полнотекстовый поиск через GraphQL API.", "Фильтры тем", "Фильтрация по нескольким темам из 30+ категорий.", "Подробная аналитика", "README, участники, анализ зависимостей.", "Авторизация GitHub", "Войдите, чтобы отмечать репозитории звёздами.", "Дизайн Material 3", "Красивый современный интерфейс.", "Без рекламы. Без отслеживания. Открытый исходный код."),
    },
    "si-LK": {
        "title": APP_TITLE,
        "short_desc": "Discover trending GitHub repos with smart search and filters.",
        "captions": {C_WEEK: "Trending This Week", C_YEAR: "Top Repos of the Year", C_PICK: "Filter by Topic", C_FILT: "Smart Topic Filters", C_DETAIL: "Deep Repo Insights"},
        "full_desc": None,
    },
    "sk": {
        "title": APP_TITLE,
        "short_desc": "Objavte popularne GitHub repozitare s inteligentnym vyhladavanim.",
        "captions": {C_WEEK: "Populárne tento týždeň", C_YEAR: "Najlepšie repozitáre roka", C_PICK: "Filtrovať podľa témy", C_FILT: "Inteligentné filtre tém", C_DETAIL: "Podrobné prehľady"},
        "full_desc": _full("GitHub Trending Explorer je najrychlejší spôsob, ako objaviť populárne projekty na GitHube.", "Hlavné funkcie:", "Objavovanie trendov", "Prehliadajte populárne repozitáre denne, týždenne, mesačne a ročne.", "Inteligentné vyhľadávanie", "Fulltextové vyhľadávanie cez GraphQL API.", "Filtre tém", "Filtrujte podľa viacerých tém s 30+ kategóriami.", "Podrobné prehľady", "README, prispievatelia, analýza závislostí.", "GitHub autentifikácia", "Prihláste sa na označovanie repozitárov hviezdičkou.", "Material 3 dizajn", "Krásne, moderné rozhranie.", "Bez reklám. Bez sledovania. Open source."),
    },
    "sl": {
        "title": APP_TITLE,
        "short_desc": "Odkrijte priljubljene GitHub repozitorije s pametnim iskanjem.",
        "captions": {C_WEEK: "Priljubljeno ta teden", C_YEAR: "Najboljsi repozitoriji leta", C_PICK: "Filtriraj po temi", C_FILT: "Pametni filtri tem", C_DETAIL: "Podroben vpogled"},
        "full_desc": None,
    },
    "sq": {
        "title": APP_TITLE,
        "short_desc": "Zbuloni depot GitHub ne trend me kerkim inteligjent.",
        "captions": {C_WEEK: "Ne trend kete jave", C_YEAR: "Depot me te mira te vitit", C_PICK: "Filtro sipas temes", C_FILT: "Filtra inteligjente", C_DETAIL: "Informacion i detajuar"},
        "full_desc": None,
    },
    "sr": {
        "title": APP_TITLE,
        "short_desc": "Откријте популарне GitHub репозиторијуме паметном претрагом.",
        "captions": {C_WEEK: "Популарно ове недеље", C_YEAR: "Најбољи репо године", C_PICK: "Филтер по теми", C_FILT: "Паметни филтери", C_DETAIL: "Детаљне информације"},
        "full_desc": None,
    },
    "sv-SE": {
        "title": APP_TITLE,
        "short_desc": "Upptack populara GitHub-repon med smart sokning och filter.",
        "captions": {C_WEEK: "Populärt denna vecka", C_YEAR: "Årets bästa repon", C_PICK: "Filtrera efter ämne", C_FILT: "Smarta ämnesfilter", C_DETAIL: "Djupgående insikter"},
        "full_desc": _full("GitHub Trending Explorer ar det snabbaste sattet att upptacka populara projekt pa GitHub.", "Huvudfunktioner:", "Upptack trender", "Bladdra bland populara repon dagligen, veckovis, manadsvis och arsvis.", "Smart sokning", "Fulltextsokning via GraphQL API.", "Amnesfilter", "Filtrera efter flera amnen med 30+ kategorier.", "Djupgaende insikter", "README, bidragsgivare, beroendeanalys.", "GitHub-autentisering", "Logga in for att stjarnmarka repon.", "Material 3-design", "Vackert, modernt granssnitt.", "Inga annonser. Ingen sparning. Oppen kallkod."),
    },
    "sw": {
        "title": APP_TITLE,
        "short_desc": "Gundua repos za GitHub zinazovuma kwa utafutaji bora.",
        "captions": {C_WEEK: "Trending wiki hii", C_YEAR: "Repos bora za mwaka", C_PICK: "Chuja kwa mada", C_FILT: "Vichujio vya mada", C_DETAIL: "Maelezo ya kina"},
        "full_desc": None,
    },
    "ta-IN": {
        "title": APP_TITLE,
        "short_desc": "GitHub trending repos smart search and topic filters.",
        "captions": {C_WEEK: "Trending This Week", C_YEAR: "Top Repos of the Year", C_PICK: "Filter by Topic", C_FILT: "Smart Topic Filters", C_DETAIL: "Deep Repo Insights"},
        "full_desc": None,
    },
    "te-IN": {
        "title": APP_TITLE,
        "short_desc": "GitHub trending repos smart search and topic filters.",
        "captions": {C_WEEK: "Trending This Week", C_YEAR: "Top Repos of the Year", C_PICK: "Filter by Topic", C_FILT: "Smart Topic Filters", C_DETAIL: "Deep Repo Insights"},
        "full_desc": None,
    },
    "th": {
        "title": APP_TITLE,
        "short_desc": "ค้นพบ repo GitHub ยอดนิยมด้วยการค้นหาอัจฉริยะ",
        "captions": {C_WEEK: "ยอดนิยมสัปดาห์นี้", C_YEAR: "Repo ที่ดีที่สุดของปี", C_PICK: "กรองตามหัวข้อ", C_FILT: "ตัวกรองหัวข้ออัจฉริยะ", C_DETAIL: "ข้อมูลเชิงลึก"},
        "full_desc": _full("GitHub Trending Explorer เป็นวิธีที่เร็วที่สุดในการค้นพบโปรเจกต์ยอดนิยมบน GitHub", "คุณสมบัติหลัก:", "ค้นพบเทรนด์", "เรียกดู repo ยอดนิยมรายวัน รายสัปดาห์ รายเดือน และรายปี", "ค้นหาอัจฉริยะ", "ค้นหาข้อความเต็มผ่าน GraphQL API", "ตัวกรองหัวข้อ", "กรองตามหลายหัวข้อจากกว่า 30 หมวดหมู่", "ข้อมูลเชิงลึก", "README ผู้มีส่วนร่วม การวิเคราะห์การพึ่งพา", "การยืนยันตัวตน GitHub", "เข้าสู่ระบบเพื่อติดดาว repo", "ดีไซน์ Material 3", "อินเทอร์เฟซที่สวยงามและทันสมัย", "ไม่มีโฆษณา ไม่มีการติดตาม โอเพ่นซอร์ส"),
    },
    "tr-TR": {
        "title": APP_TITLE,
        "short_desc": "Populer GitHub depolarini akilli arama ve filtrelerle kesfet.",
        "captions": {C_WEEK: "Bu hafta popüler", C_YEAR: "Yılın en iyi depoları", C_PICK: "Konuya göre filtrele", C_FILT: "Akıllı konu filtreleri", C_DETAIL: "Detaylı içgörüler"},
        "full_desc": _full("GitHub Trending Explorer, GitHub'daki populer projeleri kesfetmenin en hizli yoludur.", "Ana Ozellikler:", "Trendleri Kesfet", "Gunluk, haftalik, aylik ve yillik populer depolari kesfet.", "Akilli Arama", "GraphQL API uzerinden tam metin arama.", "Konu Filtreleri", "30'dan fazla kategoride birden fazla konuya gore filtrele.", "Detayli Icgoruler", "README, katkida bulunanlar, bagimlilik analizi.", "GitHub Kimlik Dogrulama", "Depolari yildizlamak icin giris yap.", "Material 3 Tasarim", "Guzel, modern arayuz.", "Reklamsiz. Izleme yok. Acik kaynak."),
    },
    "uk": {
        "title": APP_TITLE,
        "short_desc": "Відкрийте популярні GitHub репозиторії з розумним пошуком.",
        "captions": {C_WEEK: "Популярне цього тижня", C_YEAR: "Найкращі репозиторії року", C_PICK: "Фільтр за темою", C_FILT: "Розумні фільтри тем", C_DETAIL: "Детальна аналітика"},
        "full_desc": _full("GitHub Trending Explorer -- найшвидший спосіб знаходити популярні проєкти на GitHub.", "Основні функції:", "Огляд трендів", "Переглядайте популярні репозиторії за день, тиждень, місяць та рік.", "Розумний пошук", "Повнотекстовий пошук через GraphQL API.", "Фільтри тем", "Фільтрація за кількома темами з 30+ категорій.", "Детальна аналітика", "README, учасники, аналіз залежностей.", "Авторизація GitHub", "Увійдіть, щоб відзначати репозиторії зірками.", "Дизайн Material 3", "Гарний сучасний інтерфейс.", "Без реклами. Без відстеження. Відкритий код."),
    },
    "ur": {
        "title": APP_TITLE,
        "short_desc": "GitHub trending repos smart search and topic filters.",
        "captions": {C_WEEK: "Trending This Week", C_YEAR: "Top Repos of the Year", C_PICK: "Filter by Topic", C_FILT: "Smart Topic Filters", C_DETAIL: "Deep Repo Insights"},
        "full_desc": None,
    },
    "uz": {
        "title": APP_TITLE,
        "short_desc": "GitHub trending repos smart search and topic filters.",
        "captions": {C_WEEK: "Trending This Week", C_YEAR: "Top Repos of the Year", C_PICK: "Filter by Topic", C_FILT: "Smart Topic Filters", C_DETAIL: "Deep Repo Insights"},
        "full_desc": None,
    },
    "vi": {
        "title": APP_TITLE,
        "short_desc": "Kham pha cac kho GitHub thinh hanh voi tim kiem thong minh.",
        "captions": {C_WEEK: "Thịnh hành tuần này", C_YEAR: "Kho tốt nhất trong năm", C_PICK: "Lọc theo chủ đề", C_FILT: "Bộ lọc chủ đề thông minh", C_DETAIL: "Thông tin chi tiết"},
        "full_desc": _full("GitHub Trending Explorer la cach nhanh nhat de kham pha cac du an thinh hanh tren GitHub.", "Tinh nang chinh:", "Kham pha xu huong", "Duyet cac kho pho bien hang ngay, hang tuan, hang thang va hang nam.", "Tim kiem thong minh", "Tim kiem toan van qua GraphQL API.", "Bo loc chu de", "Loc theo nhieu chu de voi hon 30 danh muc.", "Thong tin chi tiet", "README, nguoi dong gop, phan tich phu thuoc.", "Xac thuc GitHub", "Dang nhap de gan sao cho cac kho.", "Thiet ke Material 3", "Giao dien hien dai va dep.", "Khong quang cao. Khong theo doi. Ma nguon mo."),
    },
    "zh-CN": {
        "title": "GitHub 趋势探索器",
        "short_desc": "通过智能搜索和主题筛选发现热门 GitHub 仓库。",
        "captions": {C_WEEK: "本周热门", C_YEAR: "年度最佳仓库", C_PICK: "按主题筛选", C_FILT: "智能主题筛选", C_DETAIL: "深度仓库洞察"},
        "full_desc": _full("GitHub 趋势探索器是发现 GitHub 热门项目的最快方式。", "主要功能：", "趋势发现", "按日、周、月、年浏览热门仓库。", "智能搜索", "由 GraphQL API 驱动的全文搜索。", "主题筛选", "同时按多个主题筛选，超过 30 个类别。", "深度洞察", "查看 README、贡献者、依赖分析。", "GitHub 认证", "登录直接给仓库加星。", "Material 3 设计", "现代美观的界面。", "无广告。无追踪。开源。"),
    },
    "zh-HK": {
        "title": "GitHub 趨勢探索器",
        "short_desc": "透過智能搜尋和主題篩選發現熱門 GitHub 倉庫。",
        "captions": {C_WEEK: "本週熱門", C_YEAR: "年度最佳倉庫", C_PICK: "按主題篩選", C_FILT: "智能主題篩選", C_DETAIL: "深度倉庫洞察"},
        "full_desc": None,
    },
    "zh-TW": {
        "title": "GitHub 趨勢探索器",
        "short_desc": "透過智慧搜尋和主題篩選發現熱門 GitHub 儲存庫。",
        "captions": {C_WEEK: "本週熱門", C_YEAR: "年度最佳儲存庫", C_PICK: "按主題篩選", C_FILT: "智慧主題篩選", C_DETAIL: "深度儲存庫洞察"},
        "full_desc": None,
    },
    "zu": {
        "title": APP_TITLE,
        "short_desc": "Thola ama-repos e-GitHub athandwayo ngokusesha okuhlakaniphile.",
        "captions": {C_WEEK: "Okuthandwayo kule wiki", C_YEAR: "Ama-repos angcono onyaka", C_PICK: "Hlunga ngesihloko", C_FILT: "Izihluzi ezihlakaniphile", C_DETAIL: "Ulwazi olunzulu"},
        "full_desc": None,
    },
}

# RTL locales that need bidi reshaping
RTL_LOCALES = {"ar", "fa", "iw-IL", "ur"}

# Font selection by script
CJK_LOCALES = {"zh-CN", "zh-HK", "zh-TW", "ja-JP", "ko-KR"}
THAI_LOCALES = {"th"}
DEVANAGARI_LOCALES = {"hi-IN", "mr-IN", "ne-NP"}
BENGALI_LOCALES = {"bn-BD"}


def find_font(size):
    for p in ["C:/Windows/Fonts/segoeui.ttf", "C:/Windows/Fonts/arial.ttf"]:
        if os.path.exists(p):
            return ImageFont.truetype(p, size)
    return ImageFont.load_default()


def find_bold_font(size):
    for p in ["C:/Windows/Fonts/segoeuib.ttf", "C:/Windows/Fonts/arialbd.ttf"]:
        if os.path.exists(p):
            return ImageFont.truetype(p, size)
    return find_font(size)


def get_font(locale, size):
    if locale in CJK_LOCALES:
        for p in ["C:/Windows/Fonts/msyh.ttc", "C:/Windows/Fonts/malgun.ttf"]:
            if os.path.exists(p):
                return ImageFont.truetype(p, size)
    if locale in RTL_LOCALES:
        for p in ["C:/Windows/Fonts/tahoma.ttf", "C:/Windows/Fonts/arial.ttf"]:
            if os.path.exists(p):
                return ImageFont.truetype(p, size)
    if locale in DEVANAGARI_LOCALES or locale in BENGALI_LOCALES:
        for p in ["C:/Windows/Fonts/Nirmala.ttc"]:
            if os.path.exists(p):
                return ImageFont.truetype(p, size)
    if locale in THAI_LOCALES:
        for p in ["C:/Windows/Fonts/tahoma.ttf", "C:/Windows/Fonts/Leelawui.ttf"]:
            if os.path.exists(p):
                return ImageFont.truetype(p, size)
    # Cyrillic, Latin, Greek - all covered by Segoe UI
    return find_bold_font(size)


def process_bidi(text, locale):
    if locale in RTL_LOCALES:
        reshaped = arabic_reshaper.reshape(text)
        return get_display(reshaped)
    return text


def create_framed_screenshot(src, caption, locale, dst):
    orig = Image.open(src).convert("RGBA")
    img = Image.new("RGBA", (FINAL_WIDTH, FINAL_HEIGHT), BG_COLOR)
    draw = ImageDraw.Draw(img)
    draw.rectangle([0, 0, FINAL_WIDTH, CAPTION_HEIGHT], fill=BG_COLOR)

    display_caption = process_bidi(caption, locale)
    font = get_font(locale, 56)
    bbox = draw.textbbox((0, 0), display_caption, font=font)
    text_w = bbox[2] - bbox[0]
    text_x = (FINAL_WIDTH - text_w) // 2
    text_y = (CAPTION_HEIGHT - (bbox[3] - bbox[1])) // 2
    draw.text((text_x, text_y), display_caption, fill=TEXT_COLOR, font=font)

    available_h = FINAL_HEIGHT - CAPTION_HEIGHT
    scale = min(FINAL_WIDTH / orig.width, available_h / orig.height)
    new_w = int(orig.width * scale)
    new_h = int(orig.height * scale)
    resized = orig.resize((new_w, new_h), Image.LANCZOS)
    paste_x = (FINAL_WIDTH - new_w) // 2
    paste_y = CAPTION_HEIGHT + (available_h - new_h) // 2
    img.paste(resized, (paste_x, paste_y), resized)

    dst.parent.mkdir(parents=True, exist_ok=True)
    img.convert("RGB").save(dst, "PNG", optimize=True)


def ensure_short_desc_fits(text):
    """Ensure short_desc fits in 80 bytes UTF-8."""
    while len(text.encode("utf-8")) > 80:
        text = text[:-1]
    return text.rstrip()


def main():
    en_full = (FASTLANE_DIR / "en-US" / "full_description.txt").read_text(encoding="utf-8")

    for locale, data in TRANSLATIONS.items():
        print(f"[{locale}]", end=" ", flush=True)
        locale_dir = FASTLANE_DIR / locale
        locale_dir.mkdir(parents=True, exist_ok=True)

        # Write metadata
        (locale_dir / "title.txt").write_text(data["title"], encoding="utf-8")
        short = ensure_short_desc_fits(data["short_desc"])
        (locale_dir / "short_description.txt").write_text(short, encoding="utf-8")

        full = data.get("full_desc")
        if full:
            (locale_dir / "full_description.txt").write_text(full, encoding="utf-8")
        elif not (locale_dir / "full_description.txt").exists():
            # Copy en-US full description as fallback
            (locale_dir / "full_description.txt").write_text(en_full, encoding="utf-8")

        # Generate screenshots
        ss_dir = locale_dir / "images" / "phoneScreenshots"
        ss_dir.mkdir(parents=True, exist_ok=True)
        for fname, caption in data["captions"].items():
            src = SCREENSHOTS_DIR / fname
            if src.exists():
                create_framed_screenshot(src, caption, locale, ss_dir / fname)

        print("OK")

    print(f"\nDone! {len(TRANSLATIONS)} locales generated.")


if __name__ == "__main__":
    main()
