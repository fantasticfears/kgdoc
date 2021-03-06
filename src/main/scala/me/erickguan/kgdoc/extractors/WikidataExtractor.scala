package me.erickguan.kgdoc.extractors

import me.erickguan.kgdoc.json._
import me.erickguan.kgdoc.processors.WikiSite

case class Triple(subject: String, predicate: String, `object`: String)
case class ItemLangLiteral(item: String, literal: String, lang: String)
case class WikiSiteLink(item: String, site: String, title: String)
object Triple {
  def repr(t: Triple, separator: Char = '\t'): String =
    s"${t.subject}$separator${t.predicate}$separator${t.`object`}"
}
object ItemLangLiteral {
  def repr(i: ItemLangLiteral, separator: Char = '\t'): String =
    s"${i.item}$separator${i.literal}$separator${i.lang}"
}

object WikiSiteLink {
  def repr(l: WikiSiteLink, separator: Char = '\t'): Iterable[String] = {
    for { url <- WikiSite.url(l.site, l.title) } yield
      s"${l.item}$separator${l.site}$separator${l.title}$separator$url"
  }
}

object WikidataExtractor {
  def triples(item: WikidataItem): Iterable[Triple] = {
    import me.erickguan.kgdoc.filters.WikidataItemFilter

    val f = new WikidataItemFilter(item)
    for {
      (p, c) <- item.claims
      if f.isEntity
      claim <- c
      if WikidataItemFilter.entityFromClaim.isDefinedAt(claim)
    } yield Triple(item.id, p, WikidataItemFilter.entityFromClaim(claim))
  }

  def aliases(item: WikidataItem): Iterable[ItemLangLiteral] = {
    for {
      a <- item.aliases
      langItem <- a._2
    } yield ItemLangLiteral(item.id, langItem.value, langItem.language)
  }

  def labels(item: WikidataItem): Iterable[ItemLangLiteral] = {
    for {
      l <- item.labels
      langItem = l._2
    } yield ItemLangLiteral(item.id, langItem.value, langItem.language)
  }

  def labelLiterals(item: WikidataItem): Iterable[ItemLangLiteral] = {
    labels(item) ++ aliases(item)
  }

  def descriptions(item: WikidataItem): Iterable[ItemLangLiteral] = {
    for {
      d <- item.descriptions
      langItem = d._2
    } yield ItemLangLiteral(item.id, langItem.value, langItem.language)
  }

  def sitelinks(item: WikidataItem): Iterable[WikiSiteLink] = {
    val m = item.sitelinks.getOrElse(Map())
    for {
      (_, siteLink) <- m
    } yield WikiSiteLink(item.id, siteLink.site, siteLink.title)
  }

}
