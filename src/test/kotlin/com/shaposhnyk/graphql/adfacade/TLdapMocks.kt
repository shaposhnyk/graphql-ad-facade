package com.shaposhnyk.graphql.adfacade

import org.springframework.LdapDataEntry
import org.springframework.ldap.core.*
import org.springframework.ldap.filter.Filter
import org.springframework.ldap.odm.core.ObjectDirectoryMapper
import org.springframework.ldap.query.LdapQuery
import java.util.*
import javax.naming.Name
import javax.naming.directory.Attributes
import javax.naming.directory.ModificationItem
import javax.naming.directory.SearchControls

class LdapOpMock(val result: List<LdapDataEntry>) : LdapOperations {
    override fun <T : Any?> search(query: LdapQuery?, mapper: ContextMapper<T>?): MutableList<T> {
        return search("", "", mapper)
    }

    override fun <T : Any?> search(query: LdapQuery?, mapper: AttributesMapper<T>?): MutableList<T> {
        return search("", "", mapper)
    }

    override fun <T : Any?> search(base: String?, filter: String?, controls: SearchControls?, mapper: AttributesMapper<T>?, processor: DirContextProcessor?): MutableList<T> {
        return result.toMutableList() as MutableList<T>
    }

    override fun <T : Any?> search(base: String?, filter: String?, mapper: ContextMapper<T>?): MutableList<T> {
        return result.toMutableList() as MutableList<T>
    }

    override fun <T : Any?> search(base: String?, filter: String?, controls: SearchControls?, mapper: ContextMapper<T>?): MutableList<T> {
        return search(base, filter, mapper)
    }

    override fun <T : Any?> search(base: Name?, filter: String?, mapper: ContextMapper<T>?): MutableList<T> {
        return search(base.toString(), filter, mapper)
    }

    override fun <T : Any?> search(base: Name?, filter: String?, controls: SearchControls?, mapper: ContextMapper<T>?): MutableList<T> {
        return search(base.toString(), filter, mapper)
    }

    override fun listBindings(base: String?, handler: NameClassPairCallbackHandler?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun listBindings(base: Name?, handler: NameClassPairCallbackHandler?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> listBindings(base: String?, mapper: NameClassPairMapper<T>?): MutableList<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> listBindings(base: Name?, mapper: NameClassPairMapper<T>?): MutableList<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun listBindings(base: String?): MutableList<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun listBindings(base: Name?): MutableList<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> listBindings(base: String?, mapper: ContextMapper<T>?): MutableList<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> listBindings(base: Name?, mapper: ContextMapper<T>?): MutableList<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun list(base: String?, handler: NameClassPairCallbackHandler?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun list(base: Name?, handler: NameClassPairCallbackHandler?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> list(base: String?, mapper: NameClassPairMapper<T>?): MutableList<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> list(base: Name?, mapper: NameClassPairMapper<T>?): MutableList<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun list(base: String?): MutableList<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun list(base: Name?): MutableList<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun lookupContext(dn: Name?): DirContextOperations {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun lookupContext(dn: String?): DirContextOperations {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> executeReadWrite(ce: ContextExecutor<T>?): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> searchForObject(base: Name?, filter: String?, mapper: ContextMapper<T>?): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> searchForObject(base: Name?, filter: String?, searchControls: SearchControls?, mapper: ContextMapper<T>?): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> searchForObject(base: String?, filter: String?, searchControls: SearchControls?, mapper: ContextMapper<T>?): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> searchForObject(base: String?, filter: String?, mapper: ContextMapper<T>?): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> searchForObject(query: LdapQuery?, mapper: ContextMapper<T>?): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun search(se: SearchExecutor?, handler: NameClassPairCallbackHandler?, processor: DirContextProcessor?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun search(se: SearchExecutor?, handler: NameClassPairCallbackHandler?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun search(base: Name?, filter: String?, controls: SearchControls?, handler: NameClassPairCallbackHandler?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun search(base: String?, filter: String?, controls: SearchControls?, handler: NameClassPairCallbackHandler?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun search(base: Name?, filter: String?, controls: SearchControls?, handler: NameClassPairCallbackHandler?, processor: DirContextProcessor?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> search(base: Name?, filter: String?, controls: SearchControls?, mapper: AttributesMapper<T>?, processor: DirContextProcessor?): MutableList<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> search(base: String?, filter: String?, controls: SearchControls?, mapper: ContextMapper<T>?, processor: DirContextProcessor?): MutableList<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> search(base: Name?, filter: String?, controls: SearchControls?, mapper: ContextMapper<T>?, processor: DirContextProcessor?): MutableList<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun search(base: String?, filter: String?, controls: SearchControls?, handler: NameClassPairCallbackHandler?, processor: DirContextProcessor?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun search(base: Name?, filter: String?, searchScope: Int, returningObjFlag: Boolean, handler: NameClassPairCallbackHandler?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun search(base: String?, filter: String?, searchScope: Int, returningObjFlag: Boolean, handler: NameClassPairCallbackHandler?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun search(base: Name?, filter: String?, handler: NameClassPairCallbackHandler?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun search(base: String?, filter: String?, handler: NameClassPairCallbackHandler?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> search(base: Name?, filter: String?, searchScope: Int, attrs: Array<out String>?, mapper: AttributesMapper<T>?): MutableList<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> search(base: String?, filter: String?, searchScope: Int, attrs: Array<out String>?, mapper: AttributesMapper<T>?): MutableList<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> search(base: Name?, filter: String?, searchScope: Int, mapper: AttributesMapper<T>?): MutableList<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> search(base: String?, filter: String?, searchScope: Int, mapper: AttributesMapper<T>?): MutableList<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> search(base: Name?, filter: String?, mapper: AttributesMapper<T>?): MutableList<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> search(base: String?, filter: String?, mapper: AttributesMapper<T>?): MutableList<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> search(base: Name?, filter: String?, searchScope: Int, attrs: Array<out String>?, mapper: ContextMapper<T>?): MutableList<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> search(base: String?, filter: String?, searchScope: Int, attrs: Array<out String>?, mapper: ContextMapper<T>?): MutableList<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> search(base: Name?, filter: String?, searchScope: Int, mapper: ContextMapper<T>?): MutableList<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> search(base: String?, filter: String?, searchScope: Int, mapper: ContextMapper<T>?): MutableList<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> search(base: String?, filter: String?, controls: SearchControls?, mapper: AttributesMapper<T>?): MutableList<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> search(base: Name?, filter: String?, controls: SearchControls?, mapper: AttributesMapper<T>?): MutableList<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun search(query: LdapQuery?, callbackHandler: NameClassPairCallbackHandler?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun create(entry: Any?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update(entry: Any?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun rename(oldDn: Name?, newDn: Name?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun rename(oldDn: String?, newDn: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun lookup(dn: Name?): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun lookup(dn: String?): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> lookup(dn: Name?, mapper: AttributesMapper<T>?): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> lookup(dn: String?, mapper: AttributesMapper<T>?): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> lookup(dn: Name?, mapper: ContextMapper<T>?): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> lookup(dn: String?, mapper: ContextMapper<T>?): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> lookup(dn: Name?, attributes: Array<out String>?, mapper: AttributesMapper<T>?): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> lookup(dn: String?, attributes: Array<out String>?, mapper: AttributesMapper<T>?): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> lookup(dn: Name?, attributes: Array<out String>?, mapper: ContextMapper<T>?): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> lookup(dn: String?, attributes: Array<out String>?, mapper: ContextMapper<T>?): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(entry: Any?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> findAll(clazz: Class<T>?): MutableList<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> findAll(base: Name?, searchControls: SearchControls?, clazz: Class<T>?): MutableList<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> executeReadOnly(ce: ContextExecutor<T>?): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun authenticate(base: Name?, filter: String?, password: String?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun authenticate(base: String?, filter: String?, password: String?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun authenticate(base: Name?, filter: String?, password: String?, callback: AuthenticatedLdapEntryContextCallback?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun authenticate(base: String?, filter: String?, password: String?, callback: AuthenticatedLdapEntryContextCallback?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun authenticate(base: Name?, filter: String?, password: String?, callback: AuthenticatedLdapEntryContextCallback?, errorCallback: AuthenticationErrorCallback?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun authenticate(base: String?, filter: String?, password: String?, callback: AuthenticatedLdapEntryContextCallback?, errorCallback: AuthenticationErrorCallback?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun authenticate(base: Name?, filter: String?, password: String?, errorCallback: AuthenticationErrorCallback?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun authenticate(base: String?, filter: String?, password: String?, errorCallback: AuthenticationErrorCallback?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> authenticate(query: LdapQuery?, password: String?, mapper: AuthenticatedLdapEntryContextMapper<T>?): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun authenticate(query: LdapQuery?, password: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun rebind(dn: Name?, obj: Any?, attributes: Attributes?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun rebind(dn: String?, obj: Any?, attributes: Attributes?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun rebind(ctx: DirContextOperations?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getObjectDirectoryMapper(): ObjectDirectoryMapper {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> find(base: Name?, filter: Filter?, searchControls: SearchControls?, clazz: Class<T>?): MutableList<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> find(query: LdapQuery?, clazz: Class<T>?): MutableList<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> findByDn(dn: Name?, clazz: Class<T>?): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unbind(dn: Name?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unbind(dn: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unbind(dn: Name?, recursive: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unbind(dn: String?, recursive: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun searchForContext(query: LdapQuery?): DirContextOperations {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> findOne(query: LdapQuery?, clazz: Class<T>?): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun modifyAttributes(dn: Name?, mods: Array<out ModificationItem>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun modifyAttributes(dn: String?, mods: Array<out ModificationItem>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun modifyAttributes(ctx: DirContextOperations?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun bind(dn: Name?, obj: Any?, attributes: Attributes?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun bind(dn: String?, obj: Any?, attributes: Attributes?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun bind(ctx: DirContextOperations?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

class LdapDn(val dnString: String) : Name {
    override fun isEmpty(): Boolean = dnString.isEmpty()

    override fun startsWith(n: Name?): Boolean = dnString.startsWith(n.toString())

    override fun endsWith(n: Name?): Boolean = dnString.endsWith(n.toString())

    override fun clone(): Any = this

    override fun size(): Int = dnString.count { it == '.' }

    override fun toString(): String {
        return dnString
    }

    override fun addAll(suffix: Name?): Name {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAll(posn: Int, n: Name?): Name {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun compareTo(other: Any?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAll(): Enumeration<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun remove(posn: Int): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun add(comp: String?): Name {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun add(posn: Int, comp: String?): Name {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPrefix(posn: Int): Name {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSuffix(posn: Int): Name {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun get(posn: Int): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class LdapMocks(private val dname: Name, private val map: Map<String, Array<String>>) : LdapDataEntry {
    override fun getDn(): Name = dname

    override fun getStringAttributes(name: String?): Array<String>? = map.get(name)

    override fun getStringAttribute(name: String?): String? = map.get(name)?.first() ?: null

    override fun attributeExists(name: String?): Boolean = map.containsKey(name)

    override fun toString(): String {
        return dname.toString()
    }

    override fun removeAttributeValue(name: String?, value: Any?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getObjectAttribute(name: String?): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setAttributeValue(name: String?, value: Any?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAttributes(): Attributes {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeValue(name: String?, value: Any?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeValue(name: String?, value: Any?, addIfDuplicateExists: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAttributeSortedStringSet(name: String?): SortedSet<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setAttributeValues(name: String?, values: Array<out Any>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setAttributeValues(name: String?, values: Array<out Any>?, orderMatters: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getObjectAttributes(name: String?): Array<Any> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        fun emptyEntry(dn: String): LdapDataEntry {
            return entry(dn, emptyMap())
        }

        fun singletonEntry(dn: String, key: String, value: String): LdapDataEntry {
            return entry(dn, mapOf(Pair(key, arrayOf(value))))
        }

        fun singletonEntry(dn: String, key: String, values: List<String>): LdapDataEntry {
            return entry(dn, mapOf(Pair(key, values.toTypedArray())))
        }

        fun entry(dn: String, map: Map<String, Array<String>>): LdapDataEntry {
            if (!map.containsKey("cn") && (dn.startsWith("cn=") || dn.startsWith("ou="))) {
                val mmap = map.toMutableMap()
                val idx = dn.indexOf(',')
                val cnValue = dn.substring(dn.indexOf('=') + 1, if (idx > 0) idx else dn.length)
                mmap["cn"] = arrayOf(cnValue)
                return LdapMocks(LdapDn(dn), mmap.toMap())
            }
            return LdapMocks(LdapDn(dn), map)
        }

        fun ldapTemplate() = LdapOpMock(emptyList())

        fun ldapTemplate(list: List<LdapDataEntry>) = LdapOpMock(list)
    }
}