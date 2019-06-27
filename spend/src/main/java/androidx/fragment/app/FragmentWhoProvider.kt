package androidx.fragment.app

fun Fragment.getWho(): String = this.mWho

fun FragmentManager.findFragmentByWho(who: String): Fragment? = (this as FragmentManagerImpl).findFragmentByWho(who)
