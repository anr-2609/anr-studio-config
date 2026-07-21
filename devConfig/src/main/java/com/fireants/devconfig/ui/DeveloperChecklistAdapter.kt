package com.fireants.devconfig.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.fireants.devconfig.R
import com.fireants.devconfig.databinding.ItemDeveloperChecklistAdBinding
import com.fireants.devconfig.databinding.ItemDeveloperChecklistInfoBinding
import com.fireants.devconfig.databinding.ItemDeveloperChecklistMediationBinding
import com.fireants.devconfig.databinding.ItemDeveloperChecklistMediationHeaderBinding
import com.fireants.devconfig.databinding.ItemDeveloperChecklistSectionBinding
import com.fireants.devconfig.utils.MediationCheckUtils

class DeveloperChecklistAdapter(
    allItems: List<DeveloperChecklistItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var allItems: List<DeveloperChecklistItem> = allItems

    private val collapsedSectionIds = mutableSetOf(
        DeveloperChecklistSectionId.AD_RELEASE
    )
    private var visibleItems: List<DeveloperChecklistItem> = computeVisibleItems()

    fun replaceItems(newItems: List<DeveloperChecklistItem>) {
        allItems = newItems
        visibleItems = computeVisibleItems()
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = when (visibleItems[position]) {
        is DeveloperChecklistItem.Section -> VIEW_TYPE_SECTION
        is DeveloperChecklistItem.InfoRow -> VIEW_TYPE_INFO
        is DeveloperChecklistItem.AdPlacement -> VIEW_TYPE_AD
        is DeveloperChecklistItem.MediationTableHeader -> VIEW_TYPE_MEDIATION_HEADER
        is DeveloperChecklistItem.MediationRow -> VIEW_TYPE_MEDIATION
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SECTION -> SectionViewHolder(
                ItemDeveloperChecklistSectionBinding.inflate(inflater, parent, false)
            )
            VIEW_TYPE_INFO -> InfoViewHolder(
                ItemDeveloperChecklistInfoBinding.inflate(inflater, parent, false)
            )
            VIEW_TYPE_MEDIATION_HEADER -> MediationHeaderViewHolder(
                ItemDeveloperChecklistMediationHeaderBinding.inflate(inflater, parent, false)
            )
            VIEW_TYPE_MEDIATION -> MediationViewHolder(
                ItemDeveloperChecklistMediationBinding.inflate(inflater, parent, false)
            )
            else -> AdViewHolder(
                ItemDeveloperChecklistAdBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = visibleItems[position]) {
            is DeveloperChecklistItem.Section -> {
                val isExpanded = item.sectionId !in collapsedSectionIds
                (holder as SectionViewHolder).bind(
                    item = item,
                    isExpanded = isExpanded,
                    onToggle = { toggleSection(item.sectionId) }
                )
            }
            is DeveloperChecklistItem.InfoRow -> (holder as InfoViewHolder).bind(item)
            is DeveloperChecklistItem.AdPlacement -> (holder as AdViewHolder).bind(item)
            is DeveloperChecklistItem.MediationTableHeader -> Unit
            is DeveloperChecklistItem.MediationRow -> (holder as MediationViewHolder).bind(item)
        }
    }

    override fun getItemCount(): Int = visibleItems.size

    private fun toggleSection(sectionId: String) {
        if (collapsedSectionIds.contains(sectionId)) {
            collapsedSectionIds.remove(sectionId)
        } else {
            collapsedSectionIds.add(sectionId)
        }
        visibleItems = computeVisibleItems()
        notifyDataSetChanged()
    }

    private fun computeVisibleItems(): List<DeveloperChecklistItem> {
        val result = mutableListOf<DeveloperChecklistItem>()
        var skipChildren = false

        for (item in allItems) {
            when (item) {
                is DeveloperChecklistItem.Section -> {
                    skipChildren = item.sectionId in collapsedSectionIds
                    result += item
                }
                else -> if (!skipChildren) result += item
            }
        }
        return result
    }

    private class SectionViewHolder(
        private val binding: ItemDeveloperChecklistSectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: DeveloperChecklistItem.Section,
            isExpanded: Boolean,
            onToggle: () -> Unit
        ) {
            val context = binding.root.context
            binding.tvSectionTitle.text = item.title
            binding.tvSectionCount.text = context.getString(
                R.string.developer_checklist_section_item_count,
                item.childCount
            )
            binding.imvExpandCollapse.setImageResource(
                if (isExpanded) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down
            )
            binding.layoutSectionHeader.setOnClickListener { onToggle() }
        }
    }

    private class InfoViewHolder(
        private val binding: ItemDeveloperChecklistInfoBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DeveloperChecklistItem.InfoRow) {
            binding.tvLabel.text = item.label
            binding.tvValue.text = item.value
        }
    }

    private class MediationViewHolder(
        private val binding: ItemDeveloperChecklistMediationBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DeveloperChecklistItem.MediationRow) {
            val context = binding.root.context
            binding.tvNetworkName.text = item.networkName
            binding.tvMediationStatus.text = item.statusLabel
            binding.tvAdapterClass.text = item.adapterClassName

            val (statusBackground, statusColor) = when (item.integrationState) {
                MediationCheckUtils.MediationIntegrationState.READY -> {
                    R.drawable.bg_mediation_status_ready to R.color.color_checklist_enabled
                }
                MediationCheckUtils.MediationIntegrationState.NOT_READY,
                MediationCheckUtils.MediationIntegrationState.SDK_ONLY -> {
                    R.drawable.bg_mediation_status_warning to R.color.color_FFA510
                }
                MediationCheckUtils.MediationIntegrationState.NOT_INTEGRATED -> {
                    R.drawable.bg_mediation_status_missing to R.color.colorDisable
                }
            }
            binding.tvMediationStatus.setBackgroundResource(statusBackground)
            binding.tvMediationStatus.setTextColor(ContextCompat.getColor(context, statusColor))

            if (item.detail.isNotBlank()) {
                binding.tvMediationDetail.visibility = View.VISIBLE
                binding.tvMediationDetail.text = item.detail
            } else {
                binding.tvMediationDetail.visibility = View.GONE
            }
        }
    }

    private class MediationHeaderViewHolder(
        binding: ItemDeveloperChecklistMediationHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root)

    private class AdViewHolder(
        private val binding: ItemDeveloperChecklistAdBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DeveloperChecklistItem.AdPlacement) {
            val context = binding.root.context
            binding.tvPlacement.text = item.placement
            binding.tvAdId.text = item.adId
            binding.tvStatus.text = if (item.isEnabled) {
                context.getString(R.string.developer_checklist_enabled)
            } else {
                context.getString(R.string.developer_checklist_disabled)
            }
            val statusColor = if (item.isEnabled) {
                R.color.color_checklist_enabled
            } else {
                R.color.colorDisable
            }
            binding.tvStatus.setTextColor(ContextCompat.getColor(context, statusColor))
        }
    }

    companion object {
        private const val VIEW_TYPE_SECTION = 0
        private const val VIEW_TYPE_INFO = 1
        private const val VIEW_TYPE_AD = 2
        private const val VIEW_TYPE_MEDIATION_HEADER = 3
        private const val VIEW_TYPE_MEDIATION = 4
    }
}
