package views.html.ublog

import controllers.routes

import lila.api.Context
import lila.app.templating.Environment._
import lila.app.ui.ScalatagsTemplate._
import lila.common.paginator.Paginator
import lila.ublog.{ UblogBlog, UblogPost }
import lila.user.User

object blog {

  import views.html.ublog.{ post => postView }

  def apply(user: User, blog: UblogBlog, posts: Paginator[UblogPost.PreviewPost])(implicit ctx: Context) = {
    val title = blog.title | "Blog"
    views.html.base.layout(
      moreCss = cssTag("ublog"),
      moreJs = frag(
        posts.hasNextPage option infiniteScrollTag,
        ctx.isAuth option jsModule("ublog")
      ),
      title = title,
      atomLinkTag = link(
        href := routes.Ublog.userAtom(user.username),
        st.title := title
      ).some
    ) {
      main(cls := "box box-pad page page-small ublog-index")(
        div(cls := "box__top")(
          h1(title),
          if (ctx is user)
            div(cls := "box__top__actions")(
              a(href := routes.Ublog.drafts(user.username))(trans.ublog.drafts()),
              postView.newPostLink
            )
          else isGranted(_.ModerateBlog) option tierForm(blog)
        ),
        standardFlash(),
        if (posts.nbResults > 0)
          div(cls := "ublog-index__posts ublog-post-cards infinite-scroll")(
            posts.currentPageResults map { postView.card(_) },
            pagerNext(posts, np => s"${routes.Ublog.index(user.username, np).url}")
          )
        else
          div(cls := "ublog-index__posts--empty")(
            trans.ublog.noPostsInThisBlogYet()
          )
      )
    }
  }

  def urlOfBlog(blog: UblogBlog) = blog.id match {
    case UblogBlog.Id.User(userId) => routes.Ublog.index(usernameOrId(userId))
  }

  private def tierForm(blog: UblogBlog) = postForm(action := routes.Ublog.setTier(blog.id.full)) {
    val form = lila.ublog.UblogForm.tier.fill(blog.tier)
    form3.select(form("tier"), lila.ublog.UblogBlog.Tier.options)
  }
}